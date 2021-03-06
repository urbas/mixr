/*
 * File name: MixR.java
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
package mixr;

import mixr.components.MixRDriver;
import mixr.components.FormulaPresenter;
import mixr.components.GoalProvider;
import mixr.components.Reasoner;
import mixr.logic.FormulaFormat;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;

/**
 * This is the main hub of the MixR framework. The only instance of this
 * class can be found through the {@link Lookup#getDefault() Lookup API}.
 *
 * <p>This class provides a catalogue of all available {@link Reasoner reasoners}
 * and {@link MixRDriver other components} that plug into MixR and
 * provide a certain kind of functionality.</p>
 *
 * <p>Additionally, this class provides central access to, for example, </p>
 *
 * {@link Reasoner Reasoners} can register themselves here, currently opened {@link GoalProvider
 * standalone reasoners} (the ones that host a proof) can also be found here,
 * additionally, the {@link RuleApplicationReasoner supporting reasoners} (the
 * ones which can apply inference steps on goals or prove them outright) will
 * be, display goals, provide formula input mechanisms etc.), currently pending
 * goals in a proof of a standalone reasoner etc.
 *
 * <p><span style="font-weight:bold">Note</span>: This class is a singleton,
 * access its only instance through the lookup API.</p>
 *
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
public interface MixR extends Provider {

    /**
     * Returns the manager of currently active goals. It is used by other
     * components to obtain the goals for various reasons (e.g., to visualise
     * the goals in different presentations, to apply inference rules on them,
     * etc.).
     *
     * @return the manager of currently active goals.
     */
    GoalsManager getGoalManager();

    /**
     * Returns the manager of reasoners currently registered to MixR.
     *
     * @return the manager of reasoners currently registered to MixR.
     */
    ReasonersManager getReasonersManager();

    /**
     * Returns the set of all registered MixR components (this set includes
     * all the registered {@link Reasoner reasoners}). <p><span
     * style="font-weight:bold">Note</span>: this method never returns {@code null}.</p>
     *
     * @return the set of all registered MixR components.
     */
    Set<? extends MixRDriver> getRegisteredComponents();
    
    /**
     * Returns the manager which keeps track of all {@link
     * FormulaFormat formula formats} known to MixR.
     * @return the manager which keeps track of all {@link
     * FormulaFormat formula formats} known to MixR.
     */
    FormulaFormatManager getFormulaFormatManager();
    
    /**
     * Returns the manager that keeps track of all {@link
     * FormulaPresenter formula presenters} known to MixR.
     * @return the manager that keeps track of all {@link
     * FormulaPresenter formula presenters} known to MixR.
     */
    PresentationManager getPresentationManager();
}
