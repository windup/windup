export const capitalizeFirstLetter = (val: string) => {
  if (!val) {
    return val;
  }

  return val.charAt(0).toUpperCase() + val.slice(1);
};
