
import java.util.ArrayList;
import java.sql.*;

public class PaymentManagement 
{
	Connection databaseConnection;
	Statement statement;
	void reconcilePayments( Connection databaseConnection )
	{
		try {
			this.databaseConnection=databaseConnection;			
			statement = databaseConnection.createStatement();
			System.out.println("database connection established");
		} catch (SQLException e) 
		{
			// SQL Exception
			System.out.println("SQL Exception");
			e.printStackTrace();
		}
	}

	boolean payOrder( Connection database, float amount, String cheque_number,ArrayList orders )
	{
		return true;
	}
	
}
