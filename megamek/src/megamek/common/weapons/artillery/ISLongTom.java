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
 * Created on Oct 20, 2004
 *
 */
package megamek.common.weapons.artillery;

import megamek.common.AmmoType;

/**
 * @author Sebastian Brocks
 */
public class ISLongTom extends ArtilleryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 5323886711682442495L;

    /**
     *
     */
    public ISLongTom() {
        super();
        name = "Long Tom";
        setInternalName("ISLongTom");
        addLookupName("ISLongTomArtillery");
        addLookupName("IS Long Tom");
        addLookupName("CLLongTom");
        addLookupName("CLLongTomArtillery");
        addLookupName("Clan Long Tom");
        heat = 20;
        rackSize = 25;
        ammoType = AmmoType.T_LONG_TOM;
        shortRange = 1;
        mediumRange = 2;
        longRange = 30;
        extremeRange = 30; // No extreme range.
        tonnage = 30f;
        criticals = 30;
        bv = 368;
        cost = 450000;
        rulesRefs = "284,TO";
        techAdvancement.setTechBase(TECH_BASE_ALL)
        .setTechRating(RATING_B)
        .setAvailability(RATING_C, RATING_C, RATING_C, RATING_C)
        .setISAdvancement(2445, 2500, 2520, DATE_NONE, DATE_NONE)
        .setISApproximate(false, false, false,false, false)
        .setClanAdvancement(2445, 2500, 2520, DATE_NONE, DATE_NONE)
        .setClanApproximate(false, false, false,false, false)
        .setPrototypeFactions(F_TH)
        .setProductionFactions(F_TH);
    }

}