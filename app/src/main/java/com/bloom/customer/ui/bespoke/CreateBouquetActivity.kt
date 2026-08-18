package com.bloom.customer.ui.bespoke

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bloom.customer.data.model.CartItem
import com.bloom.customer.data.model.Product
import com.bloom.customer.data.model.ShopInventoryItem
import com.bloom.customer.data.repository.CartRepository
import com.bloom.customer.util.SystemBarInsets
import com.bloom.databinding.ActivityCreateBouquetBinding
import io.github.sceneview.node.ModelNode
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random
import androidx.lifecycle.lifecycleScope
import dev.romainguy.kotlin.math.Float3

class CreateBouquetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBouquetBinding
    private lateinit var adapter: ShopInventoryAdapter
    private var allInventory = mutableListOf<ShopInventoryItem>()
    
    private var currentStep = "STEM"
    private var shopId: String? = null

    private var stemCount = 0
    private val activeNodes = mutableListOf<ModelNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBouquetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarInsets.apply(this)

        shopId = intent.getStringExtra("shop_id")

        binding.btnBack.setOnClickListener { finish() }
        
        setupRecyclerView()
        loadInventory()

        binding.btnNextStep.setOnClickListener { handleNextStep() }
        binding.btnMagicArrange.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        adapter = ShopInventoryAdapter()
        binding.rvInventory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvInventory.adapter = adapter
        
        adapter.setListener { item, quantityChange ->
            updateTotalPrice()
            if (quantityChange > 0) {
                add3DModel(item.colorHex ?: "", item.type)
            }
        }
    }

    private fun add3DModel(modelName: String, type: String) {
        val assetPath = "models/${modelName}.glb"
        
        lifecycleScope.launchWhenCreated {
            try {
                val modelInstance = binding.sceneView.modelLoader.loadModelInstance(assetPath)
                if (modelInstance != null) {
                    val node = ModelNode(modelInstance = modelInstance)

                    if (type == "STEM") {
                        stemCount++
                        
                        // Florist logic: Spiral Hand-Tie technique (Fibonacci Lattice)
                        if (stemCount == 1) {
                            // Focal flower, straight up at origin
                            node.position = Float3(0f, 0f, 0f)
                            node.rotation = Float3(0f, Random.nextFloat() * 360f, 0f)
                        } else {
                            val goldenAngle = 137.5f
                            val currentAngle = stemCount * goldenAngle
                            
                            // Expanding radius based on count (sqrt maintains density)
                            val radius = kotlin.math.sqrt(stemCount.toFloat()) * 0.04f
                            
                            val rad = Math.toRadians(currentAngle.toDouble()).toFloat()
                            val x = kotlin.math.cos(rad) * radius
                            val z = kotlin.math.sin(rad) * radius
                            
                            // Y drops slightly as radius increases to form a dome shape
                            val y = -(radius * 0.6f)
                            
                            node.position = Float3(x, y, z)
                            
                            // Pitch outward from center to form the volume
                            val outwardPitch = radius * 150f // e.g. max ~20-30 degrees
                            node.rotation = Float3(outwardPitch, -currentAngle, 0f)
                        }
                    } else if (type == "WRAPPER") {
                        // Place slightly behind the stems, angled backward slightly to cup the flowers
                        node.position = Float3(0f, -0.05f, -0.02f)
                        node.rotation = Float3(-5f, 0f, 0f)
                    } else if (type == "RIBBON") {
                        // Place at the exact binding point origin
                        node.position = Float3(0f, 0f, 0f)
                    }
                    
                    binding.sceneView.addChildNode(node)
                    activeNodes.add(node)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadInventory() {
        val stem1 = ShopInventoryItem().apply {
            id = "stem1"; type = "STEM"; name = "Red Rose"
            pricePerUnit = 4.50; stockQuantity = 50; colorHex = "stem_rose_red"
        }
        allInventory.add(stem1)

        val wrap1 = ShopInventoryItem().apply {
            id = "wrap1"; type = "WRAPPER"; name = "Matte Black Paper"
            pricePerUnit = 5.00; stockQuantity = 100; colorHex = "wrapper_black"
        }
        allInventory.add(wrap1)
        
        val ribbon1 = ShopInventoryItem().apply {
            id = "ribbon1"; type = "RIBBON"; name = "Silk Red Ribbon"
            pricePerUnit = 2.00; stockQuantity = 100; colorHex = "ribbon_red"
        }
        allInventory.add(ribbon1)

        filterAndShow(currentStep)
    }

    private fun filterAndShow(type: String) {
        val filtered = allInventory.filter { it.type == type }
        adapter.setItems(filtered)
    }

    private fun updateTotalPrice() {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        binding.tvTotalPrice.text = format.format(calculateTotal())
    }

    private fun calculateTotal(): Double {
        var total = 0.0
        val selections = adapter.selectedQuantities
        for (item in allInventory) {
            val qty = selections[item.id] ?: 0
            if (qty > 0) {
                total += item.pricePerUnit * qty
            }
        }
        return total + 10.0
    }

    private fun handleNextStep() {
        when (currentStep) {
            "STEM" -> {
                currentStep = "WRAPPER"
                binding.tvStepTitle.text = "Select Wrapper"
                binding.btnNextStep.text = "Review Creation"
                filterAndShow(currentStep)
            }
            "WRAPPER" -> {
                currentStep = "RIBBON"
                binding.tvStepTitle.text = "Select Ribbon"
                binding.btnNextStep.text = "Review Creation"
                filterAndShow(currentStep)
            }
            "RIBBON" -> {
                addToCart()
            }
        }
    }

    private fun addToCart() {
        if (adapter.selectedQuantities.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        binding.bottomSheet.visibility = View.GONE
        binding.llLoading.visibility = View.VISIBLE
        
        // Take a snapshot of the 3D arrangement
        val bitmap = android.graphics.Bitmap.createBitmap(
            binding.sceneView.width, binding.sceneView.height,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        
        var savedPath = "https://images.unsplash.com/photo-1582794543139-8ac9cb0f7b11?q=80&w=800"
        
        try {
            val handler = Handler(Looper.getMainLooper())
            android.view.PixelCopy.request(binding.sceneView, bitmap, { copyResult ->
                if (copyResult == android.view.PixelCopy.SUCCESS) {
                    val file = java.io.File(cacheDir, "bespoke_${System.currentTimeMillis()}.png")
                    val out = java.io.FileOutputStream(file)
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                    savedPath = "file://" + file.absolutePath
                }
                finishAddToCart(savedPath)
            }, handler)
        } catch (e: Exception) {
            e.printStackTrace()
            finishAddToCart(savedPath)
        }
    }

    private fun finishAddToCart(imagePath: String) {
        val bespokeProduct = Product().apply {
            id = "bespoke_" + System.currentTimeMillis()
            this.shopId = this@CreateBouquetActivity.shopId
            name = "Bespoke Creation (3D)"
            description = "A custom 3D arrangement made just for you."
            price = calculateTotal()
            imageUrl = imagePath
        }
        
        val cartItem = CartItem(bespokeProduct)
        CartRepository.getInstance(this).addToCart(cartItem)
        
        Toast.makeText(this, "Added to cart!", Toast.LENGTH_LONG).show()
        finish()
    }
}
