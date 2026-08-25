-- Migration: Atomic Order Creation
-- Creates place_order RPC to ensure orders and items are saved transactionally

CREATE OR REPLACE FUNCTION place_order(order_data jsonb, items_data jsonb)
RETURNS jsonb AS $$
DECLARE
    new_order_id UUID;
    result_order jsonb;
BEGIN
    -- 1. Insert into orders table
    INSERT INTO orders (
        user_id,
        shop_id,
        address_id,
        bouquet_subtotal,
        addons_subtotal,
        delivery_fee,
        platform_fee,
        tax_amount,
        discount_amount,
        total_amount,
        status,
        payment_method,
        delivery_slot,
        recipient_name,
        recipient_phone
    ) VALUES (
        (order_data->>'user_id')::UUID,
        (order_data->>'shop_id')::UUID,
        NULLIF(order_data->>'address_id', '')::UUID,
        (order_data->>'bouquet_subtotal')::DECIMAL,
        (order_data->>'addons_subtotal')::DECIMAL,
        (order_data->>'delivery_fee')::DECIMAL,
        (order_data->>'platform_fee')::DECIMAL,
        (order_data->>'tax_amount')::DECIMAL,
        (order_data->>'discount_amount')::DECIMAL,
        (order_data->>'total_amount')::DECIMAL,
        COALESCE(order_data->>'status', 'placed'),
        order_data->>'payment_method',
        order_data->>'delivery_slot',
        order_data->>'recipient_name',
        order_data->>'recipient_phone'
    ) RETURNING id INTO new_order_id;

    -- 2. Insert into order_items table using JSON array
    IF items_data IS NOT NULL THEN
        INSERT INTO order_items (
            order_id,
            product_id,
            quantity,
            unit_price,
            size,
            card_message,
            media_url
        )
        SELECT 
            new_order_id,
            (item->>'product_id')::UUID,
            (item->>'quantity')::INTEGER,
            (item->>'unit_price')::DECIMAL,
            item->>'size',
            item->>'card_message',
            item->>'media_url'
        FROM jsonb_array_elements(items_data) AS item;
    END IF;

    -- 3. Return the created order as JSON
    SELECT row_to_json(o)::jsonb INTO result_order
    FROM orders o
    WHERE o.id = new_order_id;

    RETURN result_order;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
