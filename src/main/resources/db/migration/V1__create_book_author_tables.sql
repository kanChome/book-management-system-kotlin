CREATE TABLE IF NOT EXISTS authors (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT authors_birth_date_past CHECK (birth_date < CURRENT_DATE)
);

CREATE TABLE IF NOT EXISTS books (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPUBLISHED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT books_price_non_negative CHECK (price >= 0),
    CONSTRAINT books_status_valid CHECK (status IN ('UNPUBLISHED', 'PUBLISHED'))
);

CREATE TABLE IF NOT EXISTS book_authors (
    book_id UUID NOT NULL,
    author_id UUID NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_authors_name ON authors (name);
CREATE INDEX IF NOT EXISTS idx_books_title ON books (title);
CREATE INDEX IF NOT EXISTS idx_book_authors_author_id ON book_authors (author_id);
