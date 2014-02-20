package offensive.Server.Utilities;

import java.util.List;

import offensive.Server.Server;

import org.hibernate.Session;

public class HibernateUtil {
	public static List executeHql(String hql, Session session) {
		List results = null;
		try {
			results = session.createQuery(hql).list();
		}
		catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
		
		return results;
	}
}
