import { LabelDto } from "@app/api/label";

/**
 * Takes an array of strings and convert all
 * elements with the pattern 'regex()' to an RexExp object.
 * E.g. given ['a', 'b', 'regex(myRegex)'], then return ['a', 'b', 'new RegExp("myRegex")']
 * @param array of strings
 **/
const mapRegexValues = (array: string[]) => {
  var isRegex = (value: string) =>
    value.startsWith("regex(") && value.endsWith(")");
  var getRegexValue = (value: string) =>
    value.substring(value.indexOf("regex(") + 6, value.lastIndexOf(")"));

  return array.map((value) => {
    if (isRegex(value)) {
      const regex = getRegexValue(value);
      return new RegExp(regex);
    } else if (value.endsWith("*")) {
      return new RegExp("^" + value);
    } else if (value.startsWith("*")) {
      return new RegExp(value.substr(1) + "$");
    } else {
      return value;
    }
  });
};

/**
 * @param array of strings
 * @param text which will be tested
 * @return true if some array element matches the text
 **/
const arrayMatchesText = (array: (string | RegExp)[], text: string) => {
  return array.some((regex) => {
    if (regex instanceof RegExp) {
      return regex.test(text);
    } else {
      return text === regex;
    }
  });
};

/**
 * @param array of strings; some of them can contain regexp
 * @param texts which will be tested
 * @return subgroup of elements of 'texts' which matches 'array'
 **/
const getMatchedTexts = (array: string[], texts: string[]) => {
  const mappedArray = mapRegexValues(array);

  return texts.filter((text) => {
    return arrayMatchesText(mappedArray, text);
  });
};

export interface RuntimeAssessment {
  targetRuntime: LabelDto;
  assessmentResult: "Unsuitable" | "Supported" | "Partially supported";
  assessedSupportedTags: string[];
  assessedNeutralTags: string[];
  assessedUnsuitableTags: string[];
}

export const evaluateRuntime = (
  label: LabelDto,
  tags: string[]
): RuntimeAssessment => {
  var supportedTags = getMatchedTexts(label.supported, tags);
  var neutralTags = getMatchedTexts(label.neutral, tags);
  var unsuitableTags = getMatchedTexts(label.unsuitable, tags);

  const result: RuntimeAssessment = {
    targetRuntime: { ...label },
    assessmentResult: "Unsuitable", // This value will be overwritten later
    assessedSupportedTags: supportedTags,
    assessedNeutralTags: neutralTags,
    assessedUnsuitableTags: unsuitableTags,
  };

  if (unsuitableTags.length > 0) {
    return { ...result, assessmentResult: "Unsuitable" };
  } else if (neutralTags.length + supportedTags.length === tags.length) {
    return { ...result, assessmentResult: "Supported" };
  } else {
    return { ...result, assessmentResult: "Partially supported" };
  }
};
