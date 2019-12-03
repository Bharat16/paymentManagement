use classicmodels;
 
ALTER TABLE orders
add column checkNumber varchar(50),
add column totalAmount decimal(10,2);