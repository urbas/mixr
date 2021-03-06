/*
 * File name: MixRDriver.java
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
package mixr.components;

import org.openide.util.lookup.ServiceProvider;

/**
 * This is the base type of all (interactive or non-interactive) reasoners and
 * presenters that want to be part of the MixR framework. Different
 * components will provide different functionality. Some will provide different
 * representations of formulae, others will provide fully-fledged interactive
 * proof environments.
 * 
 * <p><span style="font-weight:bold">Important</span>: All MixR components
 * can register themselves only by specifying the {@link ServiceProvider}
 * attribute with this type as the service type.</p>
 * 
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
public interface MixRDriver {
    /**
     * Returns a human-readable name of this component.
     * 
     * <p>This name will be displayed to users in the GUI.</p>
     * 
     * <p>This name should be as short and as pretty as possible. :)</p>
     * 
     * @return a human-readable name of this component.
     */
    String getName();
}
