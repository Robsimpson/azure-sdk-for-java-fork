// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.oracledatabase.generated;

/**
 * Samples for DbSystemShapes ListByLocation.
 */
public final class DbSystemShapesListByLocationSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/preview/2023-09-01-preview/examples/dbSystemShapes_listByLocation.json
     */
    /**
     * Sample code: List DbSystemShapes by location.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listDbSystemShapesByLocation(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbSystemShapes().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
