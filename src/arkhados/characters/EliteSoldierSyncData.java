package arkhados.characters;

import arkhados.controls.CEliteSoldierAmmunition;
import arkhados.messages.syncmessages.statedata.CharacterSyncData;
import arkhados.ui.hud.elitesoldier.CEliteSoldierHud;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
@Serializable
public class EliteSoldierSyncData extends CharacterSyncData {

    private byte pellets;
    private byte plasmas;
    private byte rockets;

    public EliteSoldierSyncData() {
    }

    public EliteSoldierSyncData(int id, Spatial spatial) {
        super(id, spatial);
        CEliteSoldierAmmunition ammunition = 
                spatial.getControl(CEliteSoldierAmmunition.class);
        ammunition.addAmmoSynchronizationData(this, 0);
    }

    @Override
    public void applyData(Object target) {
        super.applyData(target);
        Spatial spatial = (Spatial) target;
        CEliteSoldierAmmunition ammunition =
                spatial.getControl(CEliteSoldierAmmunition.class);
        ammunition.synchronizeAmmunition(pellets, plasmas, rockets);
        
        CEliteSoldierHud hud = spatial.getControl(CEliteSoldierHud.class);
        hud.updateAmmo(pellets, plasmas, rockets);
    }

    public void setPellets(int pellets) {
        this.pellets = (byte) pellets;
    }

    public void setPlasmas(int plasmas) {
        this.plasmas = (byte) plasmas;
    }

    public void setRockets(int rockets) {
        this.rockets = (byte) rockets;
    }
}