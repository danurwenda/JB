/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem;

import com.jbatik.lsystem.parser.exceptions.ParseRuleException;

/**
 *
 * @author RAPID02
 */
public interface VisualLSystemRenderer {

    /**
     * Generate itu menghitung ulang string linear, dipanggil saat ada perubahan
     * pada ITERASI, AXIOM atau DETAIL
     * 
     * @throws com.jbatik.lsystem.parser.exceptions.ParseRuleException
     */
    public void generate() throws ParseRuleException;
    

    /**
     * Render itu merender ulang struktur, lebih ke aspek visual, seperti ganti
     * warna, tekstur dsb
     *
     */
    public void render();

}
