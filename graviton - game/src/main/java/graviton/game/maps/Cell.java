package graviton.game.maps;

import graviton.common.Utils;
import graviton.game.client.player.Player;
import graviton.game.common.Action;
import graviton.game.creature.Creature;
import graviton.game.maps.object.InteractiveObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Botan on 22/06/2015.
 */
public class Cell {
    @Getter
    private final int id;
    @Getter
    private final Maps map;
    private final boolean walkable;
    @Getter
    private final List<Creature> creatures;
    @Setter
    private Action action;
    @Getter
    private InteractiveObject interactiveObject;

    public Cell(int id, Maps map, boolean walkable, int interactiveObject) {
        this.id = id;
        this.map = map;
        this.walkable = walkable;
        this.creatures = new CopyOnWriteArrayList<>();
        if (interactiveObject != -1)
            this.interactiveObject = new InteractiveObject(interactiveObject, map, this);
    }

    public void applyAction(Player player) {
        if (action != null)
            action.apply(player);
    }

    public boolean isWalkable() {
        if (interactiveObject != null)
            return walkable && interactiveObject.isWalkable();
        return walkable;
    }

    public void startAction(Player player, int gameAction, int action) {
        switch (action) {
            case 44: //Save position
                player.save();
                player.send("GA" + gameAction + ";" + 501 + ";" + player.getId() + ",0,0");
                player.send("Im06");
                break;
            case 102: //get water
                if (!interactiveObject.isInteractive() || interactiveObject.getState() == InteractiveObject.State.EMPTYING)
                    return;
                interactiveObject.setState(InteractiveObject.State.EMPTYING);
                interactiveObject.setInteractive(false);
                String packet = "GA" + gameAction + ";" + 501 + ";" + player.getId() + ";" + id + "," + interactiveObject.getUseDuration() + "," + interactiveObject.getUnknowValue();
                map.send(packet);
                map.send(interactiveObject.getGDF());
                break;
            case 114: //use Zaap
                player.openZaap();
                player.send("GA" + gameAction + ";" + 501 + ";" + player.getId() + ",0,0");
                break;
        }
    }

    public void finishAction(Player player, int action) {
        switch (action) {
            case 102:
                this.interactiveObject.setState(InteractiveObject.State.EMPTY);
                final int quantity = Utils.getRandomValue(1, 10);
                map.send(interactiveObject.getGDF());
                player.send("IQ" + player.getId() + "|" + quantity);
                graviton.game.object.Object newObject = player.getGameManager().getObjectTemplate(311).createObject(quantity, false);
                player.addObject(newObject);
                break;
        }
    }

    public void addCreature(Creature creature) {
        creature.getPosition().getCell().removeCreature(creature);
        this.creatures.add(creature);
        creature.getPosition().setCell(this);
    }

    public void removeCreature(Creature creature) {
        creatures.remove(creature);
    }
}
