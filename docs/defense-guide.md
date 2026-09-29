# RuMe - Defense Guide

This is separate study material. It is not part of the two-page design rationale.

## A one-minute explanation

RuMe searches for either a roommate or a tenant. Those are two different search rules, and each can retrieve profiles from any of three catalogs. Bridge separates the search hierarchy from the source hierarchy. Two catalogs support the common interface directly. The old catalog does not: it uses numeric cities, string-array records, and status codes. Adapter converts those differences into the common interface. The user chooses the source at runtime, including the adapted source.

## Trace one successful request

Input: `1`, `legacy`, `Astana`, `80000`.

`Main.main` gets the `RoommateSearch` registered under `1`. `AppRegistry.getSource` returns the object registered under `legacy`. `setSource` stores it behind `CandidateSource`. `HousingSearch.search` validates the input and calls `findByCity`. The adapter maps Astana to code 1, calls `LegacyHousingService.loadProfiles`, checks the status, parses the rows, and returns Candidate objects. The search applies budget equality. Nursultan with budget 80000 remains; Aigerim with budget 110000 does not.

With search `2`, the same source and same amount, `TenantSearch` accepts both candidates because both budgets cover the room price.

## Trace one failure

A test injects `new LegacyHousingService(false)` into the real adapter. The source returns native status 2. The adapter throws CandidateSourceException with `Candidate source is unavailable.` HousingSearch only knows this common exception, not the legacy constant. Main catches the common exception and displays the message. An empty successful result follows a different path: it is a normal empty list.

## Every production constructor and method

### Candidate

| Method or constructor | Meaning |
| --- | --- |
| `Candidate(id, name, city, budget)` | Validates positive ID and budget and nonblank name/city. Trims name and city, then stores all four fields. Invalid data causes IllegalArgumentException. |
| `getId()` | Returns the stored profile identifier. |
| `getName()` | Returns the stored name. |
| `getCity()` | Returns the stored city. |
| `getBudget()` | Returns the monthly budget in whole KZT. |
| `toString()` | Builds the readable console row containing ID, name, city, and monthly budget. |

### CandidateSource

| Method or constructor | Meaning |
| --- | --- |
| `getCode()` | Defines the unique catalog selection key, such as rume, university, or legacy. |
| `findByCity(city)` | Defines the common retrieval contract. Accepts a nonblank city; returns a non-null list of valid profiles in that city, or an empty list. Declared source failures use CandidateSourceException. There is no implementation body in this interface. |

### CandidateSourceException

| Method or constructor | Meaning |
| --- | --- |
| `CandidateSourceException(message)` | Passes a common source-error message to the Exception superclass. This checked exception is the error boundary understood by searches and the client. |

### HousingSearch

| Method or constructor | Meaning |
| --- | --- |
| `setSource(source)` | Injects the chosen Implementor into the Abstraction. Rejects null. The same search can later use a different source without changing its class. |
| `search(city, budget)` | Checks that a source exists, validates the input, trims the city, calls findByCity exactly once, then filters the returned profiles by city and the subclass budget rule. It returns a new list. The method is final so subclasses cannot bypass this common workflow. |
| `getCode()` | Abstract method requiring every search type to provide a menu key. |
| `getDescription()` | Abstract method requiring a human-readable menu label. |
| `getBudgetPrompt()` | Abstract method requiring the correct prompt for the meaning of the input amount. |
| `matchesBudget(candidateBudget, requestedBudget)` | Protected abstract operation for the variable search rule. The common search method calls it without knowing which subclass is active. |

### RoommateSearch

| Method or constructor | Meaning |
| --- | --- |
| `getCode()` | Returns 1, the roommate menu key. |
| `getDescription()` | Returns Find a roommate. |
| `getBudgetPrompt()` | Requests the user's planned monthly budget, not the full price of an apartment. |
| `matchesBudget(candidateBudget, requestedBudget)` | Returns true only when the two budgets are equal. Both lower and higher candidate budgets are excluded by the agreed rule. |

### TenantSearch

| Method or constructor | Meaning |
| --- | --- |
| `getCode()` | Returns 2, the tenant menu key. |
| `getDescription()` | Returns Find a tenant. |
| `getBudgetPrompt()` | Requests the monthly price of the available room. |
| `matchesBudget(candidateBudget, requestedBudget)` | Returns true when the candidate budget is equal to or higher than the room price. |

### RuMeCandidateSource

| Method or constructor | Meaning |
| --- | --- |
| `RuMeCandidateSource()` | Creates five fictional Candidate objects and stores them in an ArrayList. |
| `getCode()` | Returns rume. |
| `findByCity(city)` | Rejects null or blank input, loops through the list, and copies matching references into a new result list. City comparison ignores case and surrounding input spaces. There is no database or network request. |

### UniversityCandidateSource

| Method or constructor | Meaning |
| --- | --- |
| `UniversityCandidateSource()` | Creates a map from normalized city names to lists of fictional university-catalog profiles. |
| `getCode()` | Returns university. |
| `findByCity(city)` | Validates the input and normalizes the city using Locale.ROOT. Looks up the city list in the map. Returns a new copy when it exists and an empty list otherwise. |

### LegacyResult

| Method or constructor | Meaning |
| --- | --- |
| `LegacyResult(status, records)` | Stores the old service's status and raw two-dimensional string array without converting them. This is a response container, not a CandidateSource. |
| `getStatus()` | Returns the native status integer. |
| `getRecords()` | Returns the raw string matrix. The adapter, not this class, validates and converts its contents. |

### LegacyHousingService

| Method or constructor | Meaning |
| --- | --- |
| `LegacyHousingService()` | Delegates to the boolean constructor with true, so the default demonstration service is available. |
| `LegacyHousingService(available)` | Stores the availability flag. Passing false allows a real unavailable-service scenario in tests. |
| `loadProfiles(cityCode)` | Returns UNAVAILABLE when unavailable, INVALID_REQUEST for a code outside 1..3, or SUCCESS with rows matching that numeric city code. Each raw row stores ID, name, city code, and budget as strings. Returned rows are cloned so callers cannot mutate the stored records. |

### LegacyCandidateSourceAdapter

| Method or constructor | Meaning |
| --- | --- |
| `LegacyCandidateSourceAdapter()` | Creates the default old service and delegates to the injectable constructor. The public no-argument constructor allows ServiceLoader discovery. |
| `LegacyCandidateSourceAdapter(service)` | Rejects null and stores the service reference. Tests inject a stub here; the actual adapter is still executed. |
| `getCode()` | Returns legacy, making this adapter selectable like the native sources. |
| `findByCity(city)` | Validates the common input and converts a city name to a code. Unknown cities return an empty list without calling the service. A recoverable runtime failure becomes a common exception. Null responses and every nonsuccess status are translated. Successful raw rows are validated and parsed into Candidate objects; malformed rows also produce a common exception. Neither native error messages nor exception causes are exposed. |
| `toCityCode(city)` | Maps Astana to 1, Almaty to 2, and Aktobe to 3, ignoring case. Returns 0 for an unsupported city. This mapping is local to the adapter. |
| `toCityName(code)` | Converts a known numeric city code to its canonical name. An invalid code raises an exception that is caught by the adapter's record-conversion boundary. |

### AppRegistry

| Method or constructor | Meaning |
| --- | --- |
| `load()` | Uses the standard JDK ServiceLoader to discover source and search providers from resources/META-INF/services. Registers each provider. Rejects an empty provider setup. Called once at startup. |
| `registerSource(source)` | Validates the reference, normalizes its key, rejects duplicate source keys, and stores it in the source map. |
| `registerSearch(search)` | Performs the equivalent registration for search types. It does not inspect concrete subclass names. |
| `getSource(code)` | Normalizes the user's catalog input and returns the registered source. Unknown codes produce IllegalArgumentException. |
| `getSearch(code)` | Looks up a search type by its normalized menu key and rejects unknown keys. |
| `getSourceCodes()` | Returns a new list of catalog keys for the menu. |
| `getSearches()` | Returns a new list of registered search objects for displaying menu descriptions. |
| `normalize(code)` | Rejects missing or blank codes, trims surrounding spaces, and lowercases with Locale.ROOT. This makes menu selection case-insensitive and independent of the machine's language setting. |

### Main

| Method or constructor | Meaning |
| --- | --- |
| `main(args)` | Loads the registry, displays the menu, reads a search key and catalog key, injects the selected source, reads a city and amount, and calls search. It prints matching Candidate rows or a no-results message. Invalid numeric input, unsupported selections, and source failures are handled without a stack trace. The loop ends on exit at the search menu or end-of-input. Main never chooses a catalog using a hard-coded if/switch or a concrete source constructor. |

## Fields, collections, and Java syntax

| Element | Meaning in this project |
| --- | --- |
| `private` | A field or helper is accessible only inside its class. |
| `protected` | Subclasses can supply the budget rule. |
| `public` | Clients and provider loading can access the declared operation. |
| `final` field | The stored reference or value cannot be reassigned after construction. A final collection reference does not itself make the collection immutable. |
| `final` method | Subclasses cannot replace the common search workflow. |
| `abstract` class | HousingSearch cannot be directly instantiated; it supplies common behavior and requires specific operations. |
| `interface` | CandidateSource defines what catalogs provide without forcing their storage format. |
| `extends` | A search inherits from HousingSearch; the common error inherits from Exception. |
| `implements` | Each source promises to follow CandidateSource. |
| `@Override` | The compiler checks that the method implements or overrides an inherited declaration. It is an annotation, not a comment. |
| `this(...)` | Calls another constructor in the same class. |
| `super(message)` | Calls the constructor of Exception. |
| `List<Candidate>` | A type-safe list whose elements are Candidate objects. |
| `ArrayList` | A resizable list used for catalog data and results. |
| `Map` | A key-to-value lookup; used for cities and registered provider codes. |
| `LinkedHashMap` | Keeps provider insertion order stable for the menu. |
| `String[][]` | A two-dimensional array representing native legacy records. |
| `throw` / `throws` | Throw raises an exception; throws declares a checked exception that callers must handle or propagate. |
| `try` / `catch` | Defines the boundary where native failures or invalid input are converted into meaningful errors. |
| `==` and `>=` | Equality for roommate budgets; sufficient budget for tenants. |
| `ServiceLoader` | Standard JDK provider discovery. Two small metadata files list available classes; no custom reflection code or framework is used. |

## Why the architecture is not just inheritance

The source is a field of HousingSearch, not its superclass. That reference is the Bridge connection. Inheritance only creates variations of search behavior. Source implementations form a separate hierarchy through CandidateSource. Therefore two search types and three sources give six usable combinations without six combined subclasses.

The filtering workflow could also be described as template-style behavior inside the Abstraction. That does not replace the Bridge: the essential independent source reference is still present.

## Open/Closed Principle demonstration

Add a new public class implementing CandidateSource, with a public no-argument constructor and a unique code. Add its name to the source provider metadata and its path to sources.txt. Existing Java classes do not change. The new catalog is discovered and appears in the menu.

Add a new public subclass of HousingSearch in the same way, using the search provider file. Its four abstract methods provide the key, description, prompt, and rule. Existing catalogs do not change. This demonstrates the second extension axis.

Changing configuration is not the same as rewriting an existing Java class. New implementations must still respect the contract, including common failures and a non-null result list. Test-only extension classes are not production catalogs.

## Tests: how to explain them

`@Test` marks a JUnit 5 test method. `assertEquals` compares expected and actual values; `assertTrue` and `assertFalse` check conditions; `assertSame` checks object identity; `assertNull` verifies that native causes do not leak. `assertThrows` executes an operation and requires a particular exception type. The short `() -> ...` syntax passes that operation to the assertion.

A stub is a deliberately controlled replacement for a dependency. The source stub records how often and with which city it was called. The legacy stub returns chosen status codes or invalid records. The failure test uses the real adapter around the stub; replacing the adapter itself would not test its translation logic.

The following test method names describe the individual expected behaviors. A test containing a loop may check several inputs but is still one JUnit test method.

### AppRegistryTest

| Test method | Behavior under test |
| --- | --- |
| `loadsTwoSearchesAndExactlyThreeProductionSources()` | Loads Two Searches And Exactly Three Production Sources. |
| `selectsAdaptedSourceFromInput()` | Selects Adapted Source From Input. |
| `rejectsUnknownAndMissingCodes()` | Rejects Unknown And Missing Codes. |
| `rejectsDuplicateRegistrations()` | Rejects Duplicate Registrations. |
| `rejectsNullRegistrations()` | Rejects Null Registrations. |
| `supportsNewSourceAndNewAbstractionWithoutChangingRegistry()` | Supports New Source And New Abstraction Without Changing Registry. |
| `changingMenuListsDoesNotChangeRegistry()` | Changing Menu Lists Does Not Change Registry. |

### CandidateSourceTest

| Test method | Behavior under test |
| --- | --- |
| `sourcesSupportCaseInsensitiveTrimmedCities()` | Sources Support Case Insensitive Trimmed Cities. |
| `unknownCityReturnsEmptyList()` | Unknown City Returns Empty List. |
| `nullAndBlankCityUseCommonException()` | Null And Blank City Use Common Exception. |
| `modifyingReturnedListDoesNotChangeCatalog()` | Modifying Returned List Does Not Change Catalog. |
| `candidatePreservesValidFieldsAndReadableOutput()` | Candidate Preserves Valid Fields And Readable Output. |
| `candidateRejectsInvalidFields()` | Candidate Rejects Invalid Fields. |

### HousingSearchTest

| Test method | Behavior under test |
| --- | --- |
| `roommateDelegatesAndRequiresEqualBudget()` | Roommate Delegates And Requires Equal Budget. |
| `tenantDelegatesAndAcceptsEqualOrHigherBudget()` | Tenant Delegates And Accepts Equal Or Higher Budget. |
| `bothSearchesPropagateTheSourceContractError()` | Both Searches Propagate The Source Contract Error. |
| `rejectsSearchWithoutSource()` | Rejects Search Without Source. |
| `rejectsNullSource()` | Rejects Null Source. |
| `rejectsInvalidInputBeforeCallingSource()` | Rejects Invalid Input Before Calling Source. |
| `returnsEmptyListWhenBudgetDoesNotMatch()` | Returns Empty List When Budget Does Not Match. |
| `comparesCitiesWithoutCaseSensitivity()` | Compares Cities Without Case Sensitivity. |
| `bothSearchesWorkWithAllThreeSources()` | Both Searches Work With All Three Sources. |
| `sameAbstractionCanSwitchSourceAtRuntime()` | Same Abstraction Can Switch Source At Runtime. |

### LegacyCandidateSourceAdapterTest

| Test method | Behavior under test |
| --- | --- |
| `translatesCityAndStringRecords()` | Translates City And String Records. |
| `adaptsEverySupportedCity()` | Adapts Every Supported City. |
| `translatesInvalidRequestStatus()` | Translates Invalid Request Status. |
| `translatesUnavailableStatus()` | Translates Unavailable Status. |
| `translatesFailureFromRealUnavailableService()` | Translates Failure From Real Unavailable Service. |
| `translatesUnknownStatus()` | Translates Unknown Status. |
| `translatesNullResponse()` | Translates Null Response. |
| `translatesRuntimeFailureWithoutLeakingItsMessageOrCause()` | Translates Runtime Failure Without Leaking Its Message Or Cause. |
| `translatesNullRecords()` | Translates Null Records. |
| `translatesNullRecord()` | Translates Null Record. |
| `translatesWrongRecordLength()` | Translates Wrong Record Length. |
| `translatesInvalidNumericValues()` | Translates Invalid Numeric Values. |
| `translatesRecordForWrongCity()` | Translates Record For Wrong City. |
| `translatesInvalidCandidateFields()` | Translates Invalid Candidate Fields. |
| `unsupportedCityIsEmptyWithoutCallingLegacyService()` | Unsupported City Is Empty Without Calling Legacy Service. |
| `rejectsNullOrBlankCity()` | Rejects Null Or Blank City. |
| `successfulEmptyResponseIsNotAnError()` | Successful Empty Response Is Not An Error. |
| `rejectsNullWrappedService()` | Rejects Null Wrapped Service. |

### Test helper methods and fixtures

`HousingSearchTest.StubSource.getCode` returns a test key. Its `findByCity` records the city and call count, then returns predefined candidates or the configured common exception.

`LegacyCandidateSourceAdapterTest.StubLegacyService` stores a predefined response. Its overridden `loadProfiles` records the numeric city code and call count, then returns the response or throws a configured runtime exception. `assertFailure` executes the real adapter and checks the common message, absence of a native cause, and exactly one service call.

`CandidateSourceTest.sources` creates all three production sources for shared contract checks. `AppRegistryTest.ExtraSource.getCode` and `findByCity` provide a fourth, test-only catalog. `ExtraSearch` implements the four required search methods; its budget predicate uses strictly greater-than, demonstrating that the registry accepts an independently added rule.

The real JUnit engine was not run during preparation. Review validation.md and run the documented JUnit command before demonstrating a passing test suite.

## Questions to rehearse

**Why is Bridge alone insufficient?** It does not translate numeric cities, raw string rows, or native status codes.

**Why is Adapter alone insufficient?** It translates one component but does not separate catalog variation from search-rule variation.

**What makes the wrapped class genuinely incompatible?** Its method signature and parameter type, return representation, and failure mechanism differ from the common contract, not just its method name.

**Does HousingSearch know the old service?** No. It refers only to CandidateSource, Candidate, and the common exception from the source contract.

**Which complexity module is implemented?** Dynamic implementor selection. The catalog code in user input determines the selected implementation. No two-way adapter is claimed.

**Can the old component change during adaptation?** Its defined source is kept unchanged after it is introduced. Conversion belongs in the adapter; test subclasses simulate responses without editing the wrapped production class.

**Why are there no databases or APIs?** They are outside the agreed assignment scope. The three sources are educational in-memory catalogs, not claims about real external integrations.

**What is one limitation?** Exact roommate-budget equality rejects candidates even when the budget difference is small. This rule was intentionally selected for the assignment and is not a production compatibility score.

**What needs to be submitted?** The Git repository link, buildable Java source, tests runnable with the documented JUnit command, matching UML image/vector, and the design rationale in PDF or Markdown. Oral defense is mandatory.
