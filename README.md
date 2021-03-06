﻿Overview 
--------

This program works on upgrading the database and overcome the shortcomming of database design.
A summary of the formatting requirements are in the csci 3901 course assignment #6 information in the course's brightspace space.

The user provides the database connection, checknumber and list of orders with amount.
The program then works on linking the orders table to payments table.



Files and external data
-----------------------

There are three main files:
  - MainInvoicePayment.java  		-- Class which contains the main function and is used to execute the program.
  - PaymentManagement.java 			-- Class that is responsible for linking orders with payments, displaying unpaid orders 
									   and unlinked check numbers and paying unpaid orders. Thus, upgrading the databse design.
  - Problem3.sql  					-- It contains sql commands to alter table and add columns checkNumber and totalAmount.
									   It is necessary to run this file before executing the main java file.

The directory contains following versions of the code:

directory version -- Updated code to work on bluenose.cs.dal.ca


Data structures and their relations to each other
-------------------------------------------------
-Arraylists orders, customers used to store list of orders and customers.

Methods
-------

	-reconcilePayments(Connection database)
		Upgrade the database to a new design and connect orders to payments
	-payOrder(Connection database,float amount ,String cheque_number,ArrayList<Integer> orders)
		Accept database connection,cheque amount,cheque number and the list of orders as input from the user that are to be paid by the given cheque
	-unpaidOrders(Connection database)
		List the orders that have not been paired up with a cheque after database upgrade
	-unknownPayments(Connection database)
		List the cheques that have not been paired up with an order


Assumptions
-----------

  - No past order was partially paid and no past order was paid in installments.
  - Orders are paid in full or not at all.
  - An order can't be prepaid.
  - Customers can only pay for there orders.
  
Choices
-------

  - User can pay for a list of orders with a single cheque number.
  - User can check which orders dont have any payment recorded against them.
  - User can check which check numbers havent been paired up with any order in the database.


Key algorithms and design elements
----------------------------------

Establish a connection with the database using the default JDBC ODBC MySQL driver
 - reconcilePayments() 
	this method upgrades the database design and links the orders table with payments table.
	First we calculate the total value of each order and update the totalAmount column of orders
	Next we compare the totalAmount of each cheque with the cheque value in the payments table
	If an amount matches we update that cheque number against that order in the checkNumber column
	To check for multiple orders that have been paid with a single cheque number we extract a list of all the orders of a customer that have not been linked to a cheque yet then based on the orderDate we sum the orders and check the count against the amounts in the payment table for amounts that match we simply update the cheque number against the corresponding order.
	Return type is void

 - payOrder()
	this method is used to pay for provided list of orders with a single cheque number
	First the customer number is extracted from the order list provided
	then it checks if the cheque number provided is present in the payments table against the customer
	If the cheque is available it updates the checkNumber against each order provided in the orders table
	If the cheque number is not available it simply adds the cheque number,amount and customer number in the payments table
	next updates the cheque number against each order provided in the orders table
	returns true if succesful payment is recorded as else returns false
	
 - unpaidOrders()
	An sql query is executed that returns an arraylist of all the orders that have not been able to be linked by a payment.
	The result is stored in an array list and returned 
	
 - unknownPayments()
	An sql query is executed that returns an arraylist of all the cheques that have not been able to be linked with an order.
	The result is stored in an array list and returned



Limitations
-----------

-One cannot overpay or underpay for an order and adjust later.
-A customer can only pay for his order.
