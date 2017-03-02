/**
 * MegaMek - Copyright (C) 2004,2005 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */
/*
 * Created on Sep 7, 2005
 *
 */
package megamek.common.weapons.infantry;

import megamek.common.AmmoType;
import megamek.common.TechAdvancement;

/**
 * @author Ben Grills
 */
public class InfantrySupportISHeavyPulseLaserWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -3164871600230559641L;

    public InfantrySupportISHeavyPulseLaserWeapon() {
        super();

        name = "Support Pulse Laser (Heavy)";
        setInternalName(name);
        addLookupName("InfantryHeavyPulseLaser");
        addLookupName("Infantry Heavy Pulse Laser");
        addLookupName("InfantryMediumPulseLaser");
        ammoType = AmmoType.T_NA;
        cost = 60000;
        bv = 9.58;
        flags = flags.or(F_NO_FIRES).or(F_DIRECT_FIRE).or(F_ENERGY).or(F_PULSE).or(F_INF_BURST).or(F_INF_SUPPORT);
        infantryDamage = 0.98;
        infantryRange = 4;
        crew = 4;
        rulesRefs = "273, TM";
        techAdvancement.setTechBase(TechAdvancement.TECH_BASE_IS);
        techAdvancement.setISAdvancement(2605, 2615, 2675);
        techAdvancement.setTechRating(RATING_E);
        techAdvancement.setAvailability( new int[] { RATING_E, RATING_F, RATING_E, RATING_D });
    }
}