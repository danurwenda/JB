/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.filetype.lay;

import org.netbeans.spi.navigator.NavigatorLookupHint;

/**
 *
 * @author RAPID02
 */
public class LayNavigatorLookupHint implements NavigatorLookupHint{
    @Override
    public String getContentType() {
        return "text/lay+xml";
    }
}
