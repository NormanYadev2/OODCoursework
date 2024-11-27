-- Create the database
CREATE DATABASE UserManagement;

-- Use the database
USE UserManagement;

-- Create a table for users
CREATE TABLE users (
id INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL
);


select * from users;

CREATE TABLE admins (
id INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL
);

INSERT INTO admins (username, password) VALUES ('admin', 'admin123');


-- Create the Articles table
CREATE TABLE articles (
id INT AUTO_INCREMENT PRIMARY KEY,
title VARCHAR(255) NOT NULL,
content TEXT NOT NULL
);

ALTER TABLE article_views DROP FOREIGN KEY article_views_ibfk_1;
drop table articles;


LOAD DATA LOCAL INFILE 'C:\\Users\\Muralish\\Desktop\\OODCoursework\\MyOODProject\\Articles\\Article.csv'
INTO TABLE articles
FIELDS TERMINATED BY ',' -- Fields are separated by commas
ENCLOSED BY '"' -- Fields are enclosed in double quotes (if applicable)
LINES TERMINATED BY '\r\n' -- Each line is terminated by a Windows-style line ending
IGNORE 1 ROWS; -- Skip the first row if it is a header

-- Create the 'categories' table to store article categories
CREATE TABLE categories (
id INT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO categories (name) VALUES ('Technology'), ('Science'), ('Health'), ('Sports'), ('Entertainment');



CREATE TABLE article_ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    article_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    FOREIGN KEY (article_id) REFERENCES articles(id)
);

CREATE TABLE article_views (
    id INT AUTO_INCREMENT PRIMARY KEY,
    article_id INT NOT NULL,
    user_id INT, -- Assuming you have users; use NULL if not logged in
    view_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rating INT CHECK (rating BETWEEN 1 AND 5) NULL, -- NULL if not rated
    FOREIGN KEY (article_id) REFERENCES articles(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE article_views
ADD COLUMN username VARCHAR(255);
ALTER TABLE article_views
DROP COLUMN user_id;


select * from article_views;
select * from categories;

-- Correcting the table name and fetching category name
SELECT a.id AS article_id, a.title, c.name AS category
FROM article_views av
JOIN articles a ON av.article_id = a.id
JOIN categories c ON a.category_id = c.id
WHERE av.article_id = 5;  -- Replace 21 with the article_id you're interested in




ALTER TABLE articles ADD COLUMN category_id INT;

ALTER TABLE articles ADD CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(id);

select * from articles;
ALTER TABLE articles DROP FOREIGN KEY fk_category;
ALTER TABLE articles DROP COLUMN category_id;

