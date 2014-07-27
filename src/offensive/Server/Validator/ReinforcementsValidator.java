package offensive.Server.Validator;

import communication.protos.DataProtos.Command;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.Player;

public class ReinforcementsValidator extends ValidatorBase{

	@Override
	public boolean validate(CurrentGame game, Player player, Command command) {
		throw new UnsupportedOperationException();
	}
}
