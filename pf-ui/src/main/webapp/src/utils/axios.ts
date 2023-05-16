import axios from "axios";

export const API_BASE_URL = (window as any)["WINDUP_SETTINGS"].forceOnline
  ? (window as any)["WINDUP_SETTINGS"].onlineApiUrl
  : process.env.PUBLIC_URL + "/api";

export const iniAxios = () => {
  axios.defaults.baseURL = API_BASE_URL;
};
