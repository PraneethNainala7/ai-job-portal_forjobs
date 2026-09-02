/** Common job locations, focused on India with major global hubs. */
export const CITY_SUGGESTIONS = [
  "Remote",
  "Agra, India",
  "Ahmedabad, India",
  "Ajmer, India",
  "Amritsar, India",
  "Aurangabad, India",
  "Bengaluru, India",
  "Bhopal, India",
  "Bhubaneswar, India",
  "Chandigarh, India",
  "Chennai, India",
  "Coimbatore, India",
  "Dehradun, India",
  "Delhi, India",
  "Dubai, UAE",
  "Faridabad, India",
  "Ghaziabad, India",
  "Goa, India",
  "Gurugram, India",
  "Guwahati, India",
  "Gwalior, India",
  "Hyderabad, India",
  "Indore, India",
  "Jaipur, India",
  "Jammu, India",
  "Jamshedpur, India",
  "Jodhpur, India",
  "Kanpur, India",
  "Kochi, India",
  "Kolkata, India",
  "Kota, India",
  "London, UK",
  "Lucknow, India",
  "Ludhiana, India",
  "Madurai, India",
  "Mangalore, India",
  "Meerut, India",
  "Mumbai, India",
  "Mysuru, India",
  "Nagpur, India",
  "Navi Mumbai, India",
  "New York, USA",
  "Noida, India",
  "Patna, India",
  "Pondicherry, India",
  "Pune, India",
  "Raipur, India",
  "Rajkot, India",
  "Ranchi, India",
  "Salem, India",
  "San Francisco, USA",
  "Shimla, India",
  "Singapore",
  "Srinagar, India",
  "Surat, India",
  "Sydney, Australia",
  "Thane, India",
  "Thiruvananthapuram, India",
  "Tiruchirappalli, India",
  "Toronto, Canada",
  "Udaipur, India",
  "Vadodara, India",
  "Varanasi, India",
  "Vijayawada, India",
  "Visakhapatnam, India",
] as const;

/** Short filter token used in URLs; matches job locations via substring search. */
export function locationFilterToken(city: string): string {
  if (city === "Remote") return "Remote";
  const comma = city.indexOf(",");
  return comma > 0 ? city.slice(0, comma).trim() : city;
}

export function locationFilterOptions(): Array<{ value: string; label: string }> {
  return CITY_SUGGESTIONS.map((city) => ({
    value: locationFilterToken(city),
    label: city,
  }));
}

/** Normalize a location query param to a dropdown value when possible. */
export function resolveLocationFilter(value: string): string {
  if (!value) return "";
  const lower = value.toLowerCase();
  const byToken = CITY_SUGGESTIONS.find((city) => locationFilterToken(city).toLowerCase() === lower);
  if (byToken) return locationFilterToken(byToken);
  const byLabel = CITY_SUGGESTIONS.find((city) => city.toLowerCase() === lower);
  if (byLabel) return locationFilterToken(byLabel);
  return value;
}

export function filterCitySuggestions(
  query: string,
  pool: readonly string[] = CITY_SUGGESTIONS,
  limit = 8,
) {
  const needle = query.trim().toLowerCase();
  if (!needle) return pool.slice(0, limit);
  return pool.filter((city) => city.toLowerCase().includes(needle)).slice(0, limit);
}
