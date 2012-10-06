/*
 * File name: NatLang.java
 *    Author: Matej Urbas [matej.urbas@gmail.com]
 * 
 *  Copyright © 2012 Matej Urbas
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package diabelli.demo.driver.natlang;

import diabelli.components.DiabelliComponent;
import diabelli.components.FormulaFormatsProvider;
import diabelli.logic.FormulaFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * A demonstration driver which provides support for the <span
 * style="font-style:italic;">natural language</span> reasoning language.
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
@ServiceProvider(service = DiabelliComponent.class)
public class NatLang implements DiabelliComponent, FormulaFormatsProvider {

    //<editor-fold defaultstate="collapsed" desc="Diabelli Component Implementation">
    @Override
    public String getName() {
        return "NatLang";
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Formula Format Provider Implementation">
    @Override
    public Collection<FormulaFormat<?>> getFormulaFormats() {
        return FormulaFormatsContainer.FormulaFormats;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Lazy Initialisation Helpers">
    private static class FormulaFormatsContainer {

        private static final Set<FormulaFormat<?>> FormulaFormats;

        static {
            HashSet<FormulaFormat<?>> tmp = new HashSet<>();
            tmp.add(NaturalLanguage.getInstance());
            FormulaFormats = Collections.unmodifiableSet(tmp);
        }
    }
    // </editor-fold>
    
}