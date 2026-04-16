CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          name VARCHAR(150) NOT NULL,
                          description TEXT NOT NULL,

                          sku VARCHAR(100) NOT NULL UNIQUE,
                          image_url TEXT,

                          price NUMERIC(10,2) NOT NULL,
                          stock INTEGER NOT NULL,

                          status VARCHAR(30) NOT NULL,

                          category_id UUID NOT NULL,

                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                          FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_name ON products(name);