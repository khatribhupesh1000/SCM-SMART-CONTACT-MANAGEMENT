# Network Analysis — Smart Contact Manager (SCM)

## Objective

The network activity of the Smart Contact Manager (SCM) home page
was analyzed using Google Chrome DevTools.

The Network tab was opened, cache was disabled, and the SCM home page
was reloaded to observe the HTTP requests and resources loaded by the page.

## Network Conditions

- Browser: Google Chrome
- Application: Smart Contact Manager (SCM)
- URL: `http://localhost:8081`
- Cache: Disabled
- Throttling: No throttling

## Results

### 1. Total Number of Requests

The SCM home page generated **4 requests** during page loading.

### 2. Total Page Size

The Network panel reported:

- **Transferred:** 48.1 kB
- **Resources:** 250 kB

Therefore, the total transferred data during the page load was
**48.1 kB**, while the total size of the loaded resources was
**250 kB**.

### 3. Slowest Resource

The slowest resource was:

- **Resource:** `flowbite.min.js`
- **Status:** 200 OK
- **Size:** 15.0 kB
- **Time:** 33 ms

The `flowbite.min.js` file took the longest time among the four
resources loaded during the page request.

### 4. HTTP Status Codes

All four requests returned **200 OK**.

| Resource | Status | Type | Size | Time |

| `home` | 200 | document | 2.1 kB | 15 ms |
| `flowbite.min.css` | 200 | stylesheet | 17.3 kB | 32 ms |
| `output.css` | 200 | stylesheet | 13.6 kB | 4 ms |
| `flowbite.min.js` | 200 | script | 15.0 kB | 33 ms |

No **3xx** or **4xx** responses were observed during the page load.

## Additional Timing Information

- **Finish:** 60 ms
- **DOMContentLoaded:** 64 ms
- **Load:** 64 ms

## Conclusion

The SCM home page loaded four network resources successfully.
The total transferred data was 48.1 kB and the total resource size
was 250 kB.

The slowest resource was `flowbite.min.js`, which took 33 ms to load.
All requests returned HTTP 200 OK, and no 3xx or 4xx errors were observed.

This analysis provides an overview of the network requests and
resources involved in loading the Smart Contact Manager home page.
