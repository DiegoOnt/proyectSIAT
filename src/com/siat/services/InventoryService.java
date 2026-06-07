package com.siat.services;

import com.siat.dao.ActivoDAO;
import com.siat.models.Activo;

import java.util.List;

public class InventoryService {
    public static String buildSummary(List<Activo> assets) {
        long total = assets.size();
        long inService = assets.stream().filter(a -> "IN_SERVICE".equalsIgnoreCase(a.getStatus())).count();
        long maintenance = assets.stream().filter(a -> "UNDER_MAINTENANCE".equalsIgnoreCase(a.getStatus())).count();
        long retired = assets.stream().filter(a -> "RETIRED".equalsIgnoreCase(a.getStatus())).count();
        long lost = assets.stream().filter(a -> "LOST".equalsIgnoreCase(a.getStatus())).count();

        return String.format("Activos: %d | En servicio: %d | Mantenimiento: %d | Retirados: %d | Perdidos: %d", total, inService, maintenance, retired, lost);
    }
}
