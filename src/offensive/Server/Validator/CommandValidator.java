package offensive.Server.Validator;

import communication.protos.DataProtos.Command;
import offensive.Server.Exceptions.InvalidStateException;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.Phase;
import offensive.Server.Hybernate.POJO.Player;

public class CommandValidator {
	private CurrentGame game;
	
	private ValidatorBase validator;
	
	private ValidatorBase[] concreteValidators = { new ReinforcementsValidator(), new AttackValidator(), new BattleValidator(), new MoveValidator()};
	
	public CommandValidator(CurrentGame game) {
		this.game = game;
	}
	
	public boolean validate(Player player, Command command) throws InvalidStateException {
		return this.validator.validate(this.game, player, command);
	}

	public void setPhase(Phase phase) {
		this.validator = this.concreteValidators[phase.getId()];
	}
}
