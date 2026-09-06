ALTER TABLE cart_items
    ADD CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id);

ALTER TABLE cart_items
    ADD CONSTRAINT ck_cart_items_quantity_positive CHECK (quantity > 0);

ALTER TABLE cart_items
    ADD CONSTRAINT ck_cart_items_unit_price_non_negative CHECK (unit_price >= 0);
