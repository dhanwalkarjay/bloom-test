package com.bloom.customer.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.OrderItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceGenerator {

    public static Uri generateAndSaveInvoice(Context context, Order order) throws IOException {
        PdfDocument document = new PdfDocument();

        // Standard A4 dimensions in points (1/72 inch): 595 x 842
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        // Draw Premium Header Background
        paint.setColor(Color.parseColor("#111111")); // Luxury Black
        canvas.drawRect(0, 0, 595, 120, paint);

        // Header Text - Company Logo
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(36f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("BLOOM.", 40, 70, titlePaint);

        // Header Text - Invoice Label
        titlePaint.setTextSize(16f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("TAX INVOICE / RECEIPT", 400, 70, titlePaint);

        // Reset paint for body
        paint.setColor(Color.BLACK);
        paint.setTextSize(12f);

        // Hardcoded dummy corporate details (as agreed in plan)
        int startY = 160;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Bloom Luxury Floral Inc.", 40, startY, paint);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("123 Floral Avenue, Mumbai, India", 40, startY + 20, paint);
        canvas.drawText("GSTIN: 27AABCB1234C1Z5", 40, startY + 40, paint);

        // Order details (Right Aligned)
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Order ID: " + order.getId(), 555, startY, paint);
        
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        // Format Date
        String dateStr = order.getCreatedAt();
        if (dateStr == null) dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        canvas.drawText("Date: " + dateStr, 555, startY + 20, paint);
        paint.setTextAlign(Paint.Align.LEFT); // Reset
        
        // Billed To (If not addressless) - Handle Multiline!
        if (!order.isAddressless() && order.getAddress() != null) {
            canvas.drawText("Billed To: " + (order.getAddress().getRecipientName() != null ? order.getAddress().getRecipientName() : "Customer"), 40, startY + 80, paint);
            
            // Handle multi-line address properly
            android.text.TextPaint textPaint = new android.text.TextPaint(paint);
            String fullAddress = order.getAddress().getFullAddress() != null ? order.getAddress().getFullAddress() : "";
            android.text.StaticLayout staticLayout = android.text.StaticLayout.Builder.obtain(fullAddress, 0, fullAddress.length(), textPaint, 250)
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(false).build();
            
            canvas.save();
            canvas.translate(40, startY + 100);
            staticLayout.draw(canvas);
            canvas.restore();
            
        } else if (order.isAddressless()) {
            canvas.drawText("Billed To: " + (order.getRecipientName() != null ? order.getRecipientName() : "Guest"), 40, startY + 80, paint);
            canvas.drawText("Delivery via SMS Link", 40, startY + 100, paint);
        }

        // Table Header
        int tableY = startY + 200; // Pushed down to leave room for long addresses
        paint.setColor(Color.parseColor("#F5F5F5"));
        canvas.drawRect(40, tableY - 15, 555, tableY + 10, paint);
        
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Item", 50, tableY, paint);
        
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Qty", 380, tableY, paint);
        canvas.drawText("Unit Price", 460, tableY, paint);
        canvas.drawText("Total", 550, tableY, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        // Table Body
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int currentY = tableY + 30;
        
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                String title = item.getProduct() != null ? item.getProduct().getTitle() : "Bouquet";
                canvas.drawText(title, 50, currentY, paint);
                
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.valueOf(item.getQuantity()), 380, currentY, paint);
                canvas.drawText(CurrencyFormatter.format(item.getUnitPrice()), 460, currentY, paint);
                canvas.drawText(CurrencyFormatter.format(item.getUnitPrice() * item.getQuantity()), 550, currentY, paint);
                paint.setTextAlign(Paint.Align.LEFT);
                
                currentY += 30;
            }
        }

        // Divider
        currentY += 10;
        paint.setColor(Color.parseColor("#DDDDDD"));
        canvas.drawLine(40, currentY, 555, currentY, paint);

        // Totals (Right Aligned Blocks)
        currentY += 30;
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.RIGHT);
        
        canvas.drawText("Subtotal:", 460, currentY, paint);
        canvas.drawText(CurrencyFormatter.format(order.getBouquetSubtotal()), 550, currentY, paint);

        currentY += 25;
        canvas.drawText("Delivery Fee:", 460, currentY, paint);
        canvas.drawText(CurrencyFormatter.format(order.getDeliveryFee()), 550, currentY, paint);
        
        currentY += 25;
        canvas.drawText("Tax:", 460, currentY, paint);
        canvas.drawText(CurrencyFormatter.format(order.getTaxAmount()), 550, currentY, paint);

        currentY += 15;
        paint.setColor(Color.parseColor("#DDDDDD"));
        canvas.drawLine(350, currentY, 555, currentY, paint);
        
        currentY += 25;
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Grand Total:", 460, currentY, paint);
        canvas.drawText(CurrencyFormatter.format(order.getTotalAmount()), 550, currentY, paint);
        
        paint.setTextAlign(Paint.Align.LEFT); // Reset

        // Footer
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(Color.GRAY);
        paint.setTextSize(10f);
        canvas.drawText("Thank you for shopping with Bloom.", 40, 800, paint);
        canvas.drawText("This is a computer generated invoice and requires no signature.", 40, 815, paint);

        document.finishPage(page);

        // Save PDF to cache directory
        File invoicesDir = new File(context.getCacheDir(), "invoices");
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs();
        }
        
        String fileName = "Bloom_Invoice_" + order.getId() + ".pdf";
        File pdfFile = new File(invoicesDir, fileName);
        
        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();

        // Return FileProvider URI
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
    }
}
