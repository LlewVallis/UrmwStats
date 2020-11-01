import Cookies from "js-cookie";

export function requestJson(input: RequestInfo, init?: RequestInit): Promise<any> {
  return request(input, init).then(resp => resp.json());
}

export function request(input: RequestInfo, init?: RequestInit): Promise<Response> {
  return fetch(input, {
    ...init,
    headers: {
      ...init?.headers,
      "X-XSRF-TOKEN": Cookies.get("XSRF-TOKEN") || "",
    },
  }).then(resp => {
    if (resp.ok) {
      return resp;
    } else {
      throw new Error(`did not expect status ${resp.status} from request to ${input}`);
    }
  });
}