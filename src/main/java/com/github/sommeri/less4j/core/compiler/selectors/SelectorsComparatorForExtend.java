package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.problems.BugHappened;

public class SelectorsComparatorForExtend {

  private GeneralComparatorForExtend generalComparator;

  public SelectorsComparatorForExtend(GeneralComparatorForExtend generalComparator) {
    this.generalComparator = generalComparator;
  }

  //FIXME: (!!!) document - quote type must match; only form not meaning (*, order, number types) - the only exception is input type parameter - that one ignores quotes in css strings
  //FIXME (!!!) unit tests for calculated values - how it behaves if (1+1)
  //FIXME (!!!) extends - test with appenders on top replaced to nothing
  public boolean equals(Selector first, Selector second) {
    List<SelectorPart> firstParts = first.getParts();
    List<SelectorPart> secondParts = second.getParts();
    if (firstParts.size() != secondParts.size())
      return false;

    Iterator<SelectorPart> i1 = firstParts.iterator();
    Iterator<SelectorPart> i2 = secondParts.iterator();
    while (i1.hasNext()) {
      SelectorPart firstPart = i1.next();
      SelectorPart secondPart = i2.next();
      if (!selectorPartsEqual(firstPart, secondPart))
        return false;
    }
    return true;
  }

  private boolean selectorPartsEqual(SelectorPart firstPart, SelectorPart secondPart) {
    if (firstPart.getType() != ASTCssNodeType.SIMPLE_SELECTOR) {
      throw new BugHappened("Unexpected selector part type "+firstPart.getType()+". Anything but simple selector should have been removed from tree. ", firstPart);
    }
    if (secondPart.getType() != ASTCssNodeType.SIMPLE_SELECTOR) {
      throw new BugHappened("Unexpected selector part type "+secondPart.getType()+". Anything but simple selector should have been removed from tree. ", secondPart);
    }

    return simpleSelectorsEqual((SimpleSelector) firstPart, (SimpleSelector) secondPart);
  }

  private boolean simpleSelectorsEqual(SimpleSelector firstPart, SimpleSelector secondPart) {
    if (firstPart.isStar() != secondPart.isStar())
      return false;

    if (firstPart.isEmptyForm() != secondPart.isEmptyForm())
      return false;

    if (!combinatorsEqual(firstPart.getLeadingCombinator(), secondPart.getLeadingCombinator()))
      return false;

    if (!interpolableNamesEqual(firstPart.getElementName(), secondPart.getElementName()))
      return false;

    List<ElementSubsequent> firstSubsequent = firstPart.getSubsequent();
    List<ElementSubsequent> secondSubsequent = secondPart.getSubsequent();
    if (firstSubsequent.size() != secondSubsequent.size())
      return false;

    Iterator<ElementSubsequent> i1 = firstSubsequent.iterator();
    Iterator<ElementSubsequent> i2 = secondSubsequent.iterator();
    while (i1.hasNext()) {
      ElementSubsequent first = i1.next();
      ElementSubsequent second = i2.next();
      if (!subsequentsEqual(first, second))
        return false;
    }
    return true;
  }

  private boolean combinatorsEqual(SelectorCombinator cmb1, SelectorCombinator cmb2) {
    //FIXME: (!!!) this is here to solve special case, leading combinators must be rethingg
    // the problem is that 
    /* 
     * h1 {} has empty leading combinator
     * and 
     * :extend(h1) does not have
     */
    if (cmb1 == null)
      return cmb2 == null || cmb2.getCombinator() == SelectorCombinator.Combinator.DESCENDANT;

    if (cmb2 == null)
      return cmb1 == null || cmb1.getCombinator() == SelectorCombinator.Combinator.DESCENDANT;

    return cmb1.getCombinator() == cmb2.getCombinator();
  }

  private boolean subsequentsEqual(ElementSubsequent first, ElementSubsequent second) {
    if (first.getType() != second.getType()) {
      return false;
    }

    switch (first.getType()) {
    case CSS_CLASS:
    case ID_SELECTOR:
    case PSEUDO_ELEMENT: {
      return stringsEquals(first.getFullName(), second.getFullName());
    }

    case PSEUDO_CLASS: {
      PseudoClass pClass1 = (PseudoClass) first;
      PseudoClass pClass2 = (PseudoClass) second;

      return pseudoclassesEqual(pClass1, pClass2);
    }

    case SELECTOR_ATTRIBUTE: {
      SelectorAttribute attribute1 = (SelectorAttribute) first;
      SelectorAttribute attribute2 = (SelectorAttribute) second;

      return attributesEqual(attribute1, attribute2);
    }
    
    default:
      throw new BugHappened("Unexpected subsequent type: " + first.getType(), first);
    }
  }

  private boolean attributesEqual(SelectorAttribute att1, SelectorAttribute att2) {
    if (!stringsEquals(att1.getFullName(), att2.getFullName()))
      return false;

    if (!selectorOperatorsEquals(att1.getOperator(), att2.getOperator()))
      return false;

    Expression v1 = att1.getValue();
    Expression v2 = att2.getValue();

    if (v1 == null || v2 == null)
      return v1 == null && v2 == null;

    //extend keyword is "smart". It knows that [attribute=something] is the same as [attribute='something'] and the same [attribute="something"]
    String smart1 = toSmartAttributeValue(v1);
    String smart2 = toSmartAttributeValue(v2);

    if (smart1 != null && smart2 != null)
      return stringsEquals(smart1, smart2);
    
    return generalComparator.equals(v1, v2);
  }

  private String toSmartAttributeValue(Expression value) {
    switch (value.getType()) {
    case IDENTIFIER_EXPRESSION:
      return ((IdentifierExpression) value).getValue();
    case STRING_EXPRESSION:
      return ((CssString) value).getValue();
    default:
      return null;
    }
  }

  private boolean selectorOperatorsEquals(SelectorOperator operator1, SelectorOperator operator2) {
    if (operator1 == null)
      return operator2 == null;

    return operator1.getOperator() == operator2.getOperator();
  }

  private boolean pseudoclassesEqual(PseudoClass pClass1, PseudoClass pClass2) {
    if (!stringsEquals(pClass1.getFullName(), pClass2.getFullName()))
      return false;

    ASTCssNode parameter1 = pClass1.getParameter();
    ASTCssNode parameter2 = pClass2.getParameter();

    if (parameter1 == null && parameter2 == null)
      return true;

    return generalComparator.equals(parameter1, parameter2);
  }

  private boolean interpolableNamesEqual(InterpolableName firstName, InterpolableName secondName) {
    if (firstName == null)
      return secondName == null;

    if (secondName == null)
      return false;

    return stringsEquals(firstName.getName(), secondName.getName());
  }

  private boolean stringsEquals(String s1, String s2) {
    if (s1 == null || s2 == null)
      return s1 == null && s2 == null;

    return s1.equals(s2);
  }

}