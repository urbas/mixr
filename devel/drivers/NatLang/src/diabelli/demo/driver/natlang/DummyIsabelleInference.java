/*
 * File name: DummyPlaceholderInference.java
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

import diabelli.Diabelli;
import diabelli.components.GoalTransformer;
import diabelli.isabelle.terms.StringFormat;
import diabelli.logic.Formula;
import diabelli.logic.FormulaRepresentation;
import diabelli.logic.Goal;
import diabelli.logic.GoalTransformationResult;
import diabelli.logic.InferenceRule;
import diabelli.logic.InferenceRuleDescriptor;
import diabelli.logic.InferenceTarget;
import diabelli.logic.OracleProofTrace;
import diabelli.logic.Sentence;
import java.util.ArrayList;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import propity.util.MovableArrayList;

/**
 *
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
public class DummyIsabelleInference implements InferenceRuleDescriptor, InferenceRule {

    private final GoalTransformer owner;

    DummyIsabelleInference(GoalTransformer owner) {
        this.owner = owner;
    }

    @Override
    @NbBundle.Messages({
        "DII_name=Infer new Isabelle sentence"
    })
    public String getName() {
        return Bundle.DII_name();
    }

    @Override
    @NbBundle.Messages({
        "DII_description=The user types in a new Isabelle sentence. This sentence must be entailed by the original sentence."
    })
    public String getDescription() {
        return Bundle.DII_description();
    }

    @Override
    public boolean isFullyAutomated() {
        return false;
    }

    @Override
    public GoalTransformer getOwner() {
        return owner;
    }

    @Override
    public void applyInferenceRule(InferenceTarget targets) {
        String natLangSentence = getNatLangSentenceFromTarget(targets);

        if (natLangSentence != null) {
            TextInputDialog tid = new TextInputDialog(null, true);
            tid.setOriginalFormula(natLangSentence);
            tid.setVisible(true);
            if (tid.isOkay()) {
                // Put the result back to the master reasoner:
                GoalTransformationResult goalTransformationResult = new GoalTransformationResult(
                        getOwner(),
                        targets.getGoals(),
                        new MovableArrayList[]{MovableArrayList.create(new Goal(null, null, null, new Formula(StringFormat.createFormula(tid.getNewFormula()), Formula.FormulaRole.Goal)))},
                        OracleProofTrace.getInstance());
                Lookup.getDefault().lookup(Diabelli.class).getGoalManager().commitTransformedGoals(goalTransformationResult);
            }
        }
    }

    private String getNatLangSentenceFromTarget(InferenceTarget target) {
        if (target != null && target.getSentences().size() == 1) {
            Sentence sentence = target.getSentences().get(0);
            if (sentence instanceof Goal) {
                Goal goal = (Goal) sentence;
                ArrayList<? extends FormulaRepresentation> natLangRepresentations = goal.asFormula().fetchRepresentations(NaturalLanguage.getInstance());
                if (natLangRepresentations != null && !natLangRepresentations.isEmpty()) {
                    return natLangRepresentations.get(0).getFormula().toString();
                }
            }
        }
        return null;
    }
}
