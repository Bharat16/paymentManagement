import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainInvoicePayment {
	public static void main(String[] args) throws ClassNotFoundException, SQLException 
	{
		
		PaymentManagement p=new PaymentManagement();
		Connection databaseConnection =null;
		Class.forName("com.mysql.cj.jdbc.Driver");
		databaseConnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1/","root","root");		
		//p.reconcilePayments(databaseConnection);
		ArrayList<Integer> orders=p.unpaidOrders(databaseConnection);

		System.out.println(orders);
	ArrayList<String> payments=p.unknownPayments(databaseConnection);
	System.out.println(payments);
	ArrayList <Integer> payOrders=new ArrayList<Integer>();
	payOrders.add(10400);
	payOrders.add(10407);
	p.payOrder(databaseConnection,9999.99f , "EF9999", payOrders);
	}
	

}



