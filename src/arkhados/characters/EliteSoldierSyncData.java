package arkhados.characters;

import arkhados.controls.EliteSoldierAmmunitionControl;
import arkhados.messages.syncmessages.statedata.CharacterSyncData;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
@Serializable
public class EliteSoldierSyncData extends CharacterSyncData {

    private byte pellets;
    private byte bullets;
    private byte plasmas;
    private byte rockets;

    public EliteSoldierSyncData() {
    }

    public EliteSoldierSyncData(long id, Spatial spatial) {
        super(id, spatial);
        EliteSoldierAmmunitionControl ammunition = spatial.getControl(EliteSoldierAmmunitionControl.class);
        ammunition.addAmmoSynchronizationData(this, 0);
    }

    @Override
    public void applyData(Object target) {
        super.applyData(target);
        Spatial spatial = (Spatial) target;
        EliteSoldierAmmunitionControl ammunition = spatial.getControl(EliteSoldierAmmunitionControl.class);
        ammunition.synchronizeAmmunition(pellets, bullets, plasmas, rockets);
    }

    public void setPellets(int pellets) {
        this.pellets = (byte) pellets;
    }

    public void setBullets(int bullets) {
        this.bullets = (byte) bullets;
    }

    public void setPlasmas(int plasmas) {
        this.plasmas = (byte) plasmas;
    }

    public void setRockets(int rockets) {
        this.rockets = (byte) rockets;
    }
}