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
public class InfantryPistolClanNambuAutoPistolWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -3164871600230559641L;

    public InfantryPistolClanNambuAutoPistolWeapon() {
        super();

        name = "Auto-Pistol (Nambu)[Clan]";
        setInternalName(name);
        addLookupName("CLInfantryNambuAutopistol");
        addLookupName("Clan Nambu Auto Pistol");
        ammoType = AmmoType.T_NA;
        cost = 75;
        bv = 0.19;
        flags = flags.or(F_NO_FIRES).or(F_DIRECT_FIRE).or(F_BALLISTIC);
        infantryDamage = 0.21;
        infantryRange = 0;
        rulesRefs =" 273, TM";
        techAdvancement.setTechBase(TechAdvancement.TECH_BASE_CLAN);
        techAdvancement.setClanAdvancement(DATE_NONE, DATE_NONE, 2807);
        techAdvancement.setTechRating(RATING_C);
        techAdvancement.setAvailability( new int[] { RATING_X, RATING_C, RATING_C, RATING_C });
    }
}