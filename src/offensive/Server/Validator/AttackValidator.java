package offensive.Server.Validator;

import org.hibernate.Query;
import org.hibernate.Session;

import communication.protos.DataProtos.Command;
import offensive.Server.Server;
import offensive.Server.Exceptions.InvalidStateException;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.Player;

public class AttackValidator extends ValidatorBase{
	@Override
	public boolean validate(CurrentGame game, Player player, Command command) throws InvalidStateException {
		Session session = Server.getServer().sessionFactory.openSession();
		Query query = session.createQuery("FROM Connection con WHERE (con.field1.id = :src AND con.field2.id = :dst) OR (con.field1.id = :dst AND con.field2.id = :src)");
		
		query.setParameter("src", game.getTerritory(command.getSourceTerritory()).getField().getId());
		query.setParameter("dst", game.getTerritory(command.getDestinationTerritory()).getField().getId());
		
		if(query.list().size() == 0) {
			Server.getServer().logger.info(String.format("Territories %s and %s are not connected", game.getTerritory(command.getSourceTerritory()).getField().getName(), game.getTerritory(command.getDestinationTerritory()).getField().getName()));
			throw new InvalidStateException("Territories are not connected!");
		}
		
		if(game.getTerritory(command.getSourceTerritory()).getTroopsOnIt() <= command.getNumberOfUnits()) {
			throw new InvalidStateException("User does not have enough troops!!!");
		}
		
		session.close();
		
		return true;
	}
}
