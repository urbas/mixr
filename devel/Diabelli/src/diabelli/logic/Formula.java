/*
 * File name: Formula.java
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
package diabelli.logic;

import diabelli.Diabelli;
import diabelli.FormulaFormatManager;
import diabelli.components.GoalProvidingReasoner;
import diabelli.logic.FormulaTranslator.TranslationException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Represents a general formula. It can be a diagrammatic, sentential, or both
 * at the same time (if the {@link GoalProvidingReasoner reasoner} provides more
 * than one representation of this formula). This class can thus carry many
 * representations, or formats, of the same formula. For example, a formula can
 * be represented with many strings (using syntaxes of many theorem provers),
 * with term trees (abstract syntax trees), or similar.
 *
 * <p>A formula may also have the optional {@link Formula#getMainRepresentation()
 * main representation}. This representation is the original one as produced by
 * the {@link GoalProvidingReasoner goal-providing reasoner} that {@link
 * Goals#getOwner() owns} this formula. What it exactly means for a particular
 * goal-providing reasoner to have an <span style="font-style:italic;">original
 * representation</span> is up to the reasoner itself. However, the main
 * representation must logically entail (if it acts as a {@link Goal#getPremises() premise})
 * or be entailed (if it acts as a {@link Goal#getConclusion() conclusion}) by
 * all other representations.</p>
 *
 * <p><span style="font-weight:bold">Note</span>: a formula may have more than
 * one representation in a particular {@link FormulaFormatDescriptor format}.</p>
 *
 * @param <T> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
 * of {@link Formula#getMainRepresentation() the main representation} of this
 * formula.
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
@NbBundle.Messages({
    "F_toFormat_null=A target format has to be specified."
})
public class Formula<T> {

    // <editor-fold defaultstate="collapsed" desc="Fields">
    private final FormulaRepresentation<T> mainRepresentation;
    /**
     * I have decided to use a multimap of representations instead of a simple
     * map. The thing is that one there can be many representations for a single
     * format.
     *
     * <p>Detailed specification:
     *
     * <ul>
     *
     * <li>if this hash map returns {@code null} for a given format, then this
     * means that no attempt on finding a representation for this formula in the
     * given format has been made. Therefore, it is sensible to try and obtain
     * translations in this case,</li>
     *
     * <li>if this hash map returns a non-{@code null} value, then this
     * indicates that a translation attempt through {@link Goal#fetchRepresentations(diabelli.logic.Formula, diabelli.logic.FormulaFormat)}
     * has been made but no translation has been found. In this case, there is
     * no need to search for a translation again.</li>
     *
     * </ul>
     *
     * <p><span style="font-weight:bold">Important</span>: this object must be
     * used as the threading lock when fetching old or adding new
     * representations to this formula.</p>
     *
     * </p>
     */
    private final HashMap<String, HashSet<FormulaRepresentation<?>>> representationsMap;
    /**
     * This set contains all non-{@code null} representations of this formula
     * (including the main representation, if there is any).
     *
     * <p><span style="font-weight:bold">Important</span>: when using this
     * variable, you have to acquire a lock on {@link Formula#representationsMap}.
     * .</p>
     */
    private final HashSet<FormulaRepresentation<?>> representationsSet;
    private final FormulaRole role;
    /**
     * The goal that contains this formula.
     */
    private Goal hostingGoal;
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a formula with the given list of different representations.
     *
     * @param mainRepresentation the main representation of this formula.
     * <p>Other representations must be either entailed by this representation
     * (if this formula acts as a premise) or they must entail the main
     * representation (if this formula acts as a conclusion).</p>
     * @param otherRepresentations this list of representations must contain at
     * least one element. The first element of the list will become the {@link
     * Formula#getMainRepresentation() main representation}.
     * @param role the role of this formula in a {@link Goal}.
     */
    @NbBundle.Messages({
        "F_role_null=A role must be provided for this formula."
    })
    public Formula(FormulaRepresentation<T> mainRepresentation, @NonNull FormulaRole role, Collection<FormulaRepresentation<?>> otherRepresentations) {
        if (role == null) {
            throw new IllegalArgumentException(Bundle.F_role_null());
        }
        // Initialise the fields:
        this.representationsMap = new HashMap<>();
        this.representationsSet = new HashSet<>();
        this.mainRepresentation = mainRepresentation;
        if (mainRepresentation != null) {
            // Add the main representation to the registry:
            addRepresentation(mainRepresentation.getFormat(), mainRepresentation);
        }
        // Now add the other representations:
        if (otherRepresentations != null && !otherRepresentations.isEmpty()) {
            for (FormulaRepresentation<?> otherRepresentation : otherRepresentations) {
                addRepresentation(otherRepresentation);
            }
        }
        this.role = role;
    }

    /**
     * Creates a formula with the given list of different representations.
     *
     * @param mainRepresentation the main representation of this formula.
     * <p>Other representations must be either entailed by this representation
     * (if this formula acts as a premise) or they must entail the main
     * representation (if this formula acts as a conclusion).</p>
     * @param otherRepresentations this list of representations must contain at
     * least one element. The first element of the list will become the {@link
     * Formula#getMainRepresentation() main representation}.
     * @param role the role of this formula in a {@link Goal}.
     */
    public Formula(FormulaRepresentation<T> mainRepresentation, @NonNull FormulaRole role, FormulaRepresentation<?>... otherRepresentations) {
        this(mainRepresentation, role, otherRepresentations == null || otherRepresentations.length < 1 ? null : Arrays.asList(otherRepresentations));
    }

    /**
     * Creates a formula with the given list of different representations.
     *
     * @param mainRepresentation the main representation of this formula.
     * <p>Other representations must be either entailed by this representation
     * (if this formula acts as a premise) or they must entail the main
     * representation (if this formula acts as a conclusion).</p>
     * @param otherRepresentations this list of representations must contain at
     * least one element. The first element of the list will become the {@link
     * Formula#getMainRepresentation() main representation}.
     * @param role the role of this formula in a {@link Goal}.
     */
    public Formula(FormulaRepresentation<T> mainRepresentation, @NonNull FormulaRole role, ArrayList<FormulaRepresentation<?>> otherRepresentations) {
        this(mainRepresentation, role, (Collection<FormulaRepresentation<?>>) otherRepresentations);
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Public Properties">
    /**
     * Returns the main representation of this formula. This is usually the
     * native formula representation of the {@link GoalProvidingReasoner
     * reasoner} that provided this formula.
     *
     * <p>Other representations must be either entailed by this representation
     * (if this formula acts as a premise) or they must entail the main
     * representation (if this formula acts as a conclusion).</p>
     *
     * <p><span style="font-weight:bold">Note</span>: this value may be {@code
     * null}. A formula may be without a main representation. This is useful,
     * for example, in {@link Goal#getPremises() premises} of a goal. Say that
     * the premises can be represented separately, but not together in the
     * native format. However, some translations might still produce a premise
     * formula that is entailed by a subset of conjunctively connected premises.
     * This representation may be added to {@link Goal#getPremisesFormula()
     * }.</p>
     *
     * @return the main representation of this formula.
     */
    public FormulaRepresentation<T> getMainRepresentation() {
        return mainRepresentation;
    }

    /**
     * Returns the goal that contains this formula or forms its context.
     * 
     * <p>For example, particular implementations of the goal, such as
     * Isabelle's, may contain a list of globally universally quantified
     * variables. The names and types of these variables can be accessed
     * by translations or visualisations through this hosting goal.</p>
     * 
     * @return the goal that contains this formula or forms its context.
     */
    public Goal getHostingGoal() {
        return hostingGoal;
    }

    /**
     * This method returns {@code true} iff this formula has no {@link
     * Formula#getMainRepresentation() main representation} and no
     * {@link Formula#getRepresentations(diabelli.logic.FormulaFormat)
     * representations} in any other format.
     *
     * <p>A call to this method does the same as {@code getRepresentationsCount() == 0}.</p>
     *
     * @return {@code true} iff this formula has no {@link
     * Formula#getMainRepresentation() main representation} and no
     * {@link Formula#getRepresentations(diabelli.logic.FormulaFormat)
     * representations} in any other format.
     */
    public boolean isEmpty() {
        synchronized (representationsMap) {
            return representationsSet.isEmpty();
        }
    }

    /**
     * Returns the formats of all currently present/calculated representations
     * of this formula. This collection includes the main representation (if
     * present).
     *
     * <p><span style="font-weight:bold">Note</span>: this method returns only
     * those formats for which there is an actual representation of this formula
     * present.</p>
     *
     * <p>This method always returns a non-{@code null} value but it may be
     * empty.</p>
     *
     * @return all formats into which we translated the formula.
     */
    @NonNull
    public ArrayList<FormulaFormat<?>> getFormats() {
        synchronized (representationsMap) {
            ArrayList<FormulaFormat<?>> formats = new ArrayList<>();
            FormulaFormatManager formatManager = Lookup.getDefault().lookup(Diabelli.class).getFormulaFormatManager();
            for (Map.Entry<String, HashSet<FormulaRepresentation<?>>> formatEntry : representationsMap.entrySet()) {
                if (formatEntry.getValue() != null && formatEntry.getValue().size() > 0) {
                    formats.add(formatManager.getFormulaFormat(formatEntry.getKey()));
                }
            }
            return formats;
        }
    }

    /**
     * This method returns all the names of {@link FormulaFormat formula formats}
     * for which we have at least tried to get a representation of this formula.
     * This means that even if a format's name is listed in the returned
     * collection, there might be no {@link Formula#getRepresentation(diabelli.logic.FormulaFormat) representation}
     * in that formal of this formula.
     *
     * @return all the names of {@link FormulaFormat formula formats} for which
     * we have at least tried to get a representation of this formula.
     */
    public String[] getFetchedFormatNames() {
        synchronized (representationsMap) {
            Set<String> formatNames = representationsMap.keySet();
            return formatNames.toArray(new String[formatNames.size()]);
        }
    }

    /**
     * Returns the number of {@link FormulaFormat formats} this formula has been
     * tried to be translated to.
     *
     * <p>This function returns the length of the array returned by {@link Formula#getFetchedFormatNames()
     * }.</p>
     *
     * @return the number of representations this formula has.
     */
    public int getFetchedFormatsCount() {
        synchronized (representationsMap) {
            return representationsMap.size();
        }
    }

    /**
     * Returns the role of this formula in a {@link Goal}.
     *
     * @return the role of this formula in a {@link Goal}.
     */
    public FormulaRole getRole() {
        return role;
    }

    /**
     * Returns all representations of this formula (including the {@link Formula#getMainRepresentation() main representation}).
     *
     * @return all representations of this formula (including the {@link Formula#getMainRepresentation() main representation}).
     */
    public FormulaRepresentation<?>[] getRepresentations() {
        synchronized (representationsMap) {
            return representationsSet.isEmpty() ? null : representationsSet.toArray(new FormulaRepresentation<?>[representationsSet.size()]);
        }
    }

    /**
     * Returns the total number of representations of this formula. This number
     * equals to the length of the list returned by {@link Formula#getRepresentations()}.
     *
     * @return the total number of representations of this formula.
     */
    public int getRepresentationsCount() {
        synchronized (representationsMap) {
            return representationsSet.size();
        }
    }

    /**
     * Returns the representations of this formula in the given format. This
     * method does not try to convert the formula into the given format. It only
     * gives already present representations. To try and automatically calculate
     * a representation of this formula in the given format, use {@link Goal#fetchRepresentations(diabelli.logic.Formula, diabelli.logic.FormulaFormat)
     * }.
     *
     * <p>This function returns {@code null} if there are no representations of
     * this formula in the given format. Also, if this method returns a non-{@code null}
     * collection then all elements of the returned collection will be non-{@code null}
     * too.</p>
     *
     * @param <TRepresentation> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
     * carried by the returned representations.
     * @param format the desired format in which to get this formula.
     * @return the translations of the {@link Formula#getMainRepresentation()
     * formula} in the given format.
     */
    @SuppressWarnings("unchecked")
    public <TRepresentation> ArrayList<? extends FormulaRepresentation<TRepresentation>> getRepresentations(FormulaFormat<TRepresentation> format) {
        if (format == null) {
            throw new IllegalArgumentException(Bundle.F_toFormat_null());
        }
        synchronized (representationsMap) {
            HashSet<FormulaRepresentation<?>> formatReps = representationsMap.get(format.getFormatName());
            if (formatReps == null || formatReps.isEmpty()) {
                return null;
            }
            ArrayList<FormulaRepresentation<TRepresentation>> reps = new ArrayList<>();
            for (FormulaRepresentation<?> formulaRep : formatReps) {
                if (formulaRep != null) {
                    reps.add((FormulaRepresentation<TRepresentation>) formulaRep);
                }
            }
            return reps;
        }
    }

    /**
     * Returns {@code true} if an attempt has been made to translate this
     * formula into the given format.
     *
     * <p>This method will return {@code true} if, for example, the {@link Goal#fetchRepresentations(diabelli.logic.Formula, diabelli.logic.FormulaFormat)}
     * method tried to translate this formula into the given format but none of
     * the translations succeeded (they all threw a {@link TranslationException}
     * or there was no direct translation to this format from the {@link Formula#getMainRepresentation() main representation}.
     * This method is used if the {@link Goal#fetchRepresentations(diabelli.logic.Formula, diabelli.logic.FormulaFormat) fetch representations method}
     * is called multiple times&mdash;to prevent redundant translation
     * attempts.</p>
     *
     * @param format the format for which we want to check if a translation of
     * this formula to this format has been fetched already.
     * @return {@code true} if an attempt has been made to translate this
     * formula into the given format.
     */
    public boolean hasAttemptedTranslations(FormulaFormat<?> format) {
        synchronized (representationsMap) {
            return representationsMap.get(format.getFormatName()) != null;
        }
    }

    /**
     * Returns a representation of this formula in the given format.
     *
     * <p>There may be more than one representation of this formula in the given
     * format. If so, an arbitrary one is returned.</p>
     *
     * @param <TRep> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
     * carried by the returned representation.
     * @param format the desired format in which to get this formula.
     * @return the translation of the {@link Formula#getMainRepresentation()
     * formula} in the given format.
     */
    @SuppressWarnings("unchecked")
    public <TRep> FormulaRepresentation<TRep> getRepresentation(FormulaFormat<TRep> format) {
        if (format == null) {
            throw new IllegalArgumentException(Bundle.F_toFormat_null());
        }
        synchronized (representationsMap) {
            HashSet<FormulaRepresentation<?>> formatReps = representationsMap.get(format.getFormatName());
            return (FormulaRepresentation<TRep>) (formatReps == null || formatReps.isEmpty() ? null : formatReps.iterator().next());
        }
    }

    /**
     * Returns the number of representations of this formula in the given
     * format.
     *
     * @param format the format for which we want to get the number of
     * representations.
     * @return the number of representations of this formula in the given
     * format.
     */
    public int getRepresentationsCount(FormulaFormat<?> format) {
        if (format == null) {
            throw new IllegalArgumentException(Bundle.F_toFormat_null());
        }
        synchronized (representationsMap) {
            HashSet<FormulaRepresentation<?>> formatReps = representationsMap.get(format.getFormatName());
            return formatReps == null || formatReps.isEmpty() ? 0 : formatReps.size();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helper Classes">
    /**
     * Indicates the role of the {@link Formula formula} in a goal.
     */
    public static enum FormulaRole {

        /**
         * Indicates that the {@link Formula formula} is {@link
         * Goal#getPremises() a premise}.
         */
        Premise,
        /**
         * Indicates that the {@link Formula formula} is {@link
         * Goal#getConclusion() the conclusion}.
         */
        Conclusion,
        /**
         * Indicates that the {@link Formula formula} is {@link
         * Goal#asFormula() the goal itself}.
         */
        Goal;

        /**
         * Checks whether the translation of the given type is applicable on a
         * formula of this role.
         *
         * @param transType the type of the translation.
         * @return a value indicating whether the translation of the given type
         * is applicable on a formula of this role.
         */
        public boolean isTranslationApplicable(FormulaTranslator.TranslationType transType) {
            return this == Premise ? transType == FormulaTranslator.TranslationType.ToEquivalent || transType == FormulaTranslator.TranslationType.ToEntailed
                    : this == Conclusion ? transType == FormulaTranslator.TranslationType.ToEquivalent || transType == FormulaTranslator.TranslationType.ToEntailing
                    : transType == FormulaTranslator.TranslationType.ToEquivalent;
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Translation Interface">
    /**
     * First looks up if there already is a representation of this formula in
     * the given format or if it has already been attempted to convert this
     * formula to the given format. If so, then the existing list of
     * representations are returned (which might be {@code null} or empty).
     *
     * <p>However, if there was no attempt to translate this formula into the
     * given format, then an attempt will be made. If the translation was
     * successful the resulting representation will be returned, otherwise
     * {@code null} is returned.</p>
     *
     * <p>This method is thread-safe.</p>
     *
     * <p>This method is quite expensive if called for the first time,
     * successive calls will be as expensive as calls to {@link Formula#getRepresentations(diabelli.logic.FormulaFormat)}.</p>
     *
     * <p><span style="font-weight:bold">Important</span>: this method tries to
     * translate only the main representation into others. Therefore, if there
     * is no main representation, this method does the same as {@link Formula#getRepresentations(diabelli.logic.FormulaFormat)}.</p>
     *
     * @param <TTo> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
     * carried by the returned representations.
     * @param format the desired format in which to get this formula.
     * @return the translation of the {@link Formula#getMainRepresentation()
     * formula}.
     */
    public <TTo> ArrayList<? extends FormulaRepresentation<TTo>> fetchRepresentations(FormulaFormat<TTo> format) {
        if (format == null) {
            throw new IllegalArgumentException(Bundle.F_toFormat_null());
        }
        // If the representations in this format have already been calculated
        // once, return what is already available (it does not matter if no
        // translations are available).
        if (hasAttemptedTranslations(format)) {
            return getRepresentations(format);
        }
        // If there is no main representation, then we will not attempt a
        // translation at all:
        if (getMainRepresentation() == null) {
            return null;
        }
        // Try to translate this formula:
        FormulaRepresentation<TTo> representation = null;
        // There is no representation yet for this format. Try to find one.
        final Set<FormulaTranslator<T, TTo>> formulaTranslatorsFrom = Lookup.getDefault().lookup(Diabelli.class).getFormulaFormatManager().getFormulaTranslators(getMainRepresentation().getFormat(), format);
        if (formulaTranslatorsFrom != null && !formulaTranslatorsFrom.isEmpty()) {
            for (FormulaTranslator<T, TTo> translator : formulaTranslatorsFrom) {
                // Make sure that the translation is valid:
                if (getRole().isTranslationApplicable(translator.getTranslationType())) {
                    try {
                        // We can try and translate it:
                        representation = translator.translate(this);
                        if (representation != null) {
                            // We got a translation, add it to the collection of
                            // all representations of this formula and return it
                            break;
                        }
                    } catch (FormulaTranslator.TranslationException ex) {
                        Logger.getLogger(Formula.class.getName()).log(Level.FINEST, String.format("Translation with '%s' failed. Translation error message: %s", translator.getPrettyName(), ex.getMessage()), ex);
                    }
                }
            }
        }
        // Put the found representation into the collection of all representatios.
        // In case the translation didn't succeed, null will indicate that in the
        // future no automatic translation attempts need to be made.
        addRepresentation(format, representation);
        if (representation == null) {
            return null;
        } else {
            ArrayList<FormulaRepresentation<TTo>> rep = new ArrayList<>();
            rep.add(representation);
            return rep;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Private Helper Methods">
    /**
     * Adds the representation into the collection of all representations of
     * this formula.
     *
     * <p>If the given representation is {@code null}</p>
     *
     * @param <T> the new format of the representation to add.
     * @param format the format of the representation to add (must not be {@code null}).
     * @param representation
     */
    @NbBundle.Messages({
        "F_format_null=The representation to be added does not identify its format. A valid format must be provided.",
        "F_format_mismatch=The format of the representation to be added and the specified format are not the same."
    })
    private <T> void addRepresentation(FormulaFormat<T> format, FormulaRepresentation<T> representation) {
        if (format == null) {
            throw new IllegalArgumentException(Bundle.F_format_null());
        }
        synchronized (representationsMap) {
            HashSet<FormulaRepresentation<?>> formatReps = representationsMap.get(format.getFormatName());
            if (formatReps == null) {
                representationsMap.put(format.getFormatName(), formatReps = new HashSet<>());
            }
            if (representation != null) {
                if (representation.getFormat() != format) {
                    throw new IllegalStateException(Bundle.F_format_mismatch());
                }
                representation.setParentFormula(this);
                formatReps.add(representation);
                representationsSet.add(representation);
            }
        }
    }

    /**
     * Adds a new representation of this formula to the {@link Formula#getRepresentations() collection of all representations}.
     *
     * <p>This method may be used to add representations of {@link Goal#getPremises() premises}
     * into the {@link Goal#getPremisesFormula() premises formula} if the latter
     * does not have a {@link Formula#getMainRepresentation() main representation}.</p>
     *
     * <p><span style="font-weight:bold">Warning</span>: the added
     * representations must logically correspond to the original formula. For
     * example, a subset of premises converted with an {@link FormulaTranslator.TranslationType#ToEntailed entailed translation}
     * can be placed into the formula that represents premises.</p>
     *
     * @param <T> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}
     * carried by the added representation.
     * @param representation the representation of this formula.
     */
    @NbBundle.Messages({
        "F_representation_null=Only valid non-null representations can be added to a formula."
    })
    public final <T> void addRepresentation(FormulaRepresentation<T> representation) {
        if (representation == null) {
            throw new IllegalArgumentException(Bundle.F_representation_null());
        }
        addRepresentation(representation.getFormat(), representation);
    }

    /**
     * Sets the hosting goal of this formula.
     * 
     * <p>Typically, the {@link Goal goal} itself will set this value.</p>
     * 
     * @param hostingGoal the new context of this formula.
     */
    final void setHostingGoal(Goal hostingGoal) {
        this.hostingGoal = hostingGoal;
    }
    // </editor-fold>
}
