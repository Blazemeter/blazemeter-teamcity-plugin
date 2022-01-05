## Changelog

### v.2.5.138
- ADDED` : Added Report Name in Advance Option

### v.2.5.132
- `ADDED` : Server Report url for FunctionalGui tests
- `FIXED` : FunctionalGui tests always showing SUCCESS
- `FIXED` : FunctionalGui public report video generating properly

### v.2.5.*
- `FIXED` : save user keys for NOT ROOT servers

### v.2.4.126
- `ADDED` : upgrade notification
- `FIXED` : send jmeter properties to New Taurus test
- `FIXED` : interrupt build when plugin send properties/notes
- `FIXED` : Retry Interceptor. Use Java property `bzm.request.retries.count` for config retries count (by default it is 3). Will retry only `GET` requests in case if response code was not 2** or was throw `SocketTimeoutException`

### v.2.3.120 

- `FEATURE` - added BlazeMeter Report tab in build results
- `FEATURE` - added workspace selection
- `FIXED` - crashed server if config has not server information/credentials


### v.2.2.114

- `FIXED` - bzm-log file contains logs only from one Master
- `FIXED` - sorting tests by name
- `CHANGES` - update blazemeter-api-client dependency version from 1.2 to 1.8 ([changelog](https://github.com/Blazemeter/blazemeter-api-client/wiki/Changelog))

### v.2.1.104

- Migrate to [blazemeter-api-client](https://github.com/Blazemeter/blazemeter-api-client) library

### v.2.0.92

- Migration to BlazeMeter v4 API.
- [Support for new version of BlazeMeter API-keys](https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys). The old version of BlazeMeter API-keys is deprecated and NOT supported anymore!
- Ability to pass JMeter property to test session.
- Ability to download junit test report.
- Ability to download jtl report.
- Ability to add notes to test session.
- User credentials validation on administration page.
- UI changes of BlazeMeter step on View step configuration page.

### v.1.1.40

- Ability to execute BlazeMeter test.
