import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;
import java.text.DecimalFormat;

public class PaymentManagement {
	Connection database;
	Statement statement, statement1, statement2;
	ResultSet resultSet, resultSet1;

	/**
	 * Upgrade the database to a new design and connect orders to payments
	 * 
	 * @param database
	 */
	void reconcilePayments(Connection database) {
		try {
			this.database = database;
			statement = database.createStatement();
			statement1 = database.createStatement();
			statement2 = database.createStatement();
			statement.executeQuery("use classicmodels");
			// to update the database schema
			String updateQuery = "UPDATE orders\r\n" + "        INNER JOIN\r\n"
					+ "    orderdetails ON orders.ordernumber = orderdetails.ordernumber \r\n" + "SET \r\n"
					+ "    orders.totalAmount = (SELECT \r\n"
					+ "            SUM(orderdetails.quantityOrdered * orderdetails.priceEach)\r\n" + "        FROM\r\n"
					+ "            orderdetails\r\n" + "        WHERE\r\n"
					+ "            orders.ordernumber = orderdetails.ordernumber\r\n"
					+ "        GROUP BY ordernumber);";
			statement.executeUpdate(updateQuery);

			updateQuery = "UPDATE orders\r\n" + "        INNER JOIN\r\n"
					+ "    payments ON orders.customerNumber = payments.customerNumber \r\n" + "SET \r\n"
					+ "    orders.checkNumber = (SELECT \r\n" + "            checkNumber\r\n" + "        FROM\r\n"
					+ "            payments\r\n" + "        WHERE\r\n"
					+ "            orders.totalAmount = payments.amount);";
			statement.executeUpdate(updateQuery);
			//
			ArrayList<String> orders = new ArrayList<String>();
			ArrayList<String> customers = new ArrayList<String>();
			double sum = 0;
			double amount1 = 0;

			resultSet = statement.executeQuery(
					"SELECT \r\n" + "    orderNumber,\r\n" + "    SUM(quantityOrdered * priceEach) AS sum,\r\n"
							+ "    a.customerNumber AS customerNumber,\r\n" + "    a.shippedDate\r\n" + "FROM\r\n"
							+ "    orderdetails\r\n" + "        JOIN\r\n" + "    (SELECT \r\n" + "        *\r\n"
							+ "    FROM\r\n" + "        orders) AS a USING (orderNumber)\r\n" + "WHERE\r\n"
							+ "    status IN ('Shipped' , 'Resolved')\r\n" + "GROUP BY (orderNumber)\r\n"
							+ "ORDER BY customerNumber , orderDate;");
			while (resultSet.next()) {
				if (!customers.contains(resultSet.getString("customerNumber"))) {
					sum = 0;
					orders.clear();
				}
				String customerNo = resultSet.getString("customerNumber");
				String orderNumber = resultSet.getString("orderNumber");
				String amount = resultSet.getString("sum");
				String amountS = resultSet.getString("sum");
				amount1 = Double.parseDouble(amount);
				if (sum != 0) {
					amount1 = amount1 + sum;
					String a1 = new DecimalFormat("#.##").format(amount1);
					amount1 = Double.parseDouble(a1);
				}

				resultSet1 = statement1.executeQuery("SELECT checkNumber FROM payments WHERE "
						+ "payments.customerNumber = '" + customerNo + "' && payments.amount = '" + amount1 + "'");
				if (!customers.contains(customerNo)) {
					customers.add(customerNo);
				}
				if (resultSet1.next()) {
					String checkNo = resultSet1.getString("checkNumber");
					if (orders.size() > 0) {
						for (int i = 0; i < orders.size(); i++) {
							statement2.executeUpdate("UPDATE orders SET checkNumber = '" + checkNo + "' WHERE "
									+ "orderNumber = '" + orders.get(i) + "'");
						}
						orders.clear();
						sum = 0;
					}

					statement2.executeUpdate("UPDATE orders SET checkNumber = '" + checkNo + "' WHERE "
							+ "orderNumber = '" + orderNumber + "'");

				} else {
					orders.add(resultSet.getString("orderNumber"));
					sum += Double.parseDouble(amountS);
				}
			}
			//
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Records the receipt of a payment, with the given cheque number, that is
	 * supposed to cover all of the listed orders. Returns true if the payments are
	 * recorded as proper payments and false if the payments aren’t recorded
	 * 
	 * @param database
	 * @param amount
	 * @param cheque_number
	 * @param orders
	 * @return
	 */
	boolean payOrder(Connection database, float amount, String cheque_number, ArrayList<Integer> orders) {
		try {
			this.database = database;
			statement = database.createStatement();
			statement.executeQuery("use classicmodels");
			String sqlQuery = "select customerNumber from orders where orderNumber=" + orders.get(0) + ";";
			ResultSet resultSet = statement.executeQuery(sqlQuery);
			resultSet.next();
			String customerNumber = resultSet.getString("customerNumber");
			sqlQuery = "SELECT count(*) AS chequeCount FROM payments WHERE checkNumber='" + cheque_number
					+ "' AND customerNumber=" + customerNumber + ";";
			resultSet = statement.executeQuery(sqlQuery);
			resultSet.next();
			Integer chequeCount = 0;
			chequeCount = resultSet.getInt("chequeCount");
			if (chequeCount > 0) {
				for (Integer order : orders) {
					sqlQuery = "UPDATE orders SET checkNumber='" + cheque_number + "' WHERE orderNumber=" + order + ";";
					statement.executeUpdate(sqlQuery);
				}
			} else {
				sqlQuery = "INSERT INTO payments(customerNumber,checkNumber,paymentDate,amount) values("
						+ customerNumber + ",'" + cheque_number + "',CURDATE()," + amount + ")";
				statement.execute(sqlQuery);
				for (Integer order : orders) {
					sqlQuery = "UPDATE orders SET checkNumber='" + cheque_number + "' WHERE orderNumber=" + order + ";";
					statement.executeUpdate(sqlQuery);
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Returns a list of all the orders for which we have no record of a payment in
	 * the database, excluding cancelled and disputed orders.
	 * 
	 * @param database
	 * @return
	 * @throws SQLException
	 */
	ArrayList<Integer> unpaidOrders(Connection database) throws SQLException {
		try {
			this.database = database;
			statement = database.createStatement();
			statement.executeQuery("use classicmodels");
			ArrayList<Integer> orders = new ArrayList<Integer>();
			String getOrders = "SELECT \r\n" + "    orderNumber\r\n" + "FROM\r\n" + "    orders\r\n" + "WHERE\r\n"
					+ "    checkNumber IS NULL\r\n" + "        AND (status != 'cancelled'\r\n"
					+ "        OR status != 'disputed');";
			ResultSet resultSet = statement.executeQuery(getOrders);
			while (resultSet.next()) {
				orders.add(resultSet.getInt("orderNumber"));
			}
			statement.close();
			return orders;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Returns a list of all the cheque numbers that are not paired up with any of
	 * the orders in the database
	 * 
	 * @param database
	 * @return
	 */
	ArrayList<String> unknownPayments(Connection database) {
		try {
			this.database = database;
			statement = database.createStatement();
			statement.executeQuery("use classicmodels");
			ArrayList<String> payments = new ArrayList<String>();
			String getOrders = "SELECT \r\n" + "    payments.checkNumber\r\n" + "FROM\r\n" + "    orders\r\n"
					+ "        RIGHT JOIN\r\n" + "    payments ON orders.checkNumber = payments.checkNumber\r\n"
					+ "WHERE\r\n" + "    orderNumber IS NULL;";
			ResultSet resultSet = statement.executeQuery(getOrders);
			while (resultSet.next()) {
				payments.add(resultSet.getString("checkNumber"));
			}
			statement.close();
			return payments;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
}
