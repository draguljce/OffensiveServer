package offensive.Server.Validator;

import communication.protos.DataProtos.Command;
import offensive.Server.Exceptions.InvalidStateException;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.Player;

public abstract class ValidatorBase {
	public abstract boolean validate(CurrentGame game, Player player, Command command) throws InvalidStateException;
}
