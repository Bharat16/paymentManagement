import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainInvoicePayment {
	public static void main(String[] args) throws ClassNotFoundException, SQLException 
	{
		// TODO Auto-generated method stub
		PaymentManagement p=new PaymentManagement();
		Connection databaseConnection =null;
		Class.forName("com.mysql.cj.jdbc.Driver");
		databaseConnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1/classicmodels","root","root");		
		p.reconcilePayments(databaseConnection);
	}

}



