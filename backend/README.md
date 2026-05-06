# Thank You Matcher Application

The Thank You Matcher Application is a Java-based application for assigning seminar students to donors, volunteers, staff members, speakers, and other individuals who should receive thank-you letters.

The goal of the application is to produce a clean Excel spreadsheet showing which student should thank which person or organization, while honoring sponsorship and seminar assignment rules.

---

## Current Manual Process

The organization operates a leadership seminar and tracks several categories of people who should receive thank-you notes:

- Monetary donors
- Gift-in-kind donors
- Gift card donors
- Volunteer staff
- Volunteer board members
- Speakers
- Other miscellaneous thank-you recipients

During the seminar, students are assigned people to thank.

The current process uses several spreadsheets:

### 2026 Income Spreadsheet

This spreadsheet tracks incoming support.

Relevant tabs include:

- `Donations`
- `Gift in Kind`
- `Gift Cards`

Monetary donations may be earmarked. This is indicated by the `Earmarked Donation?` column.

If a donation is earmarked, the relevant sponsorship information may appear in one of these columns:

- `Sponsored School`
- `Sponsored County`
- `Sponsored JStaff`

The `Sponsored Student` column is ignored.

### Donor Info Spreadsheet

This spreadsheet is created from the income spreadsheet and contains the people or organizations to thank.

Relevant tabs include:

- `Donations`
- `Staff`
- `Speakers`

The `Donations` tab combines monetary donations, gift-in-kind donations, and gift card donations.

### Student Information Spreadsheet

This spreadsheet contains students attending the seminar.

Expected student fields include:

- `First Name`
- `Last Name`
- `School Name`
- `Color`
- `Group`

Example:

| First Name | Last Name | School Name | Color | Group |
|---|---|---|---|---|
| Farrah | VanCott | Beacon High School | Red | A |
| Brooke | Nephew | Beekmantown Central School | Red | A |
| Charlie | Goodell | Catskill High School | Red | A |

---

## Matching Rules

The application assigns students to thankable people or organizations using the following rules.

### 1. School or County Sponsorships

Some donations are earmarked for a student from a specific school or county.

If a donor sponsors a school, the thank-you assignment should go to a student from that school.

If a donor sponsors a county, the thank-you assignment should go to a student from that county.

### 2. Junior Staff Sponsorships

Some donors sponsor a junior staff member.

If the `Sponsored JStaff` column is populated, the junior staff member should thank the donor.

This thank-you is not assigned to a student.

### 3. Staff Color and Group Matching

Some volunteer staff are assigned a section color and letter group.

When possible, staff should be thanked by a student from the same color and group.

Example:

A `Red A Facilitator` should be thanked by a student in `Red A`.

### 4. Minimum Student Assignments

Each student should receive at least two thank-you assignments when enough thankable assignments exist.

The solver prefers giving students their first and second thank-you assignment before assigning a third thank-you to another student.

If the minimum cannot be met, the application reports an error instead of creating fake or invalid assignments.

### 5. Weighted Thank-Yous

Some donors may be assigned a weight.

A donor with weight `3` should receive three thank-you letters.

Weighted thankables are expanded before matching.

---

## Output Format

The application produces an Excel spreadsheet with columns similar to:

| studentName | donorOrg | donorName | donorAddress | donation |
|---|---|---|---|---|
| Amanda Smith | New Company | John Donor | 123 Main St, Fishkill NY | Monetary donation of $100 |
| Tim Walshjamin | Donor 2 | Amanda Walshjamin | 222 Snook Rd | Gift card donation |
| Colin Walshjamin | Donor 3 | Owen Walshjamin | 222 Snook Rd | Red A Facilitator |

The application will also include error reporting for issues such as:

- No valid student match
- Missing donor name
- Missing donor address
- Student did not receive the minimum number of thank-you assignments

---

## Technical Design

The backend is written in Java 21 using Spring Boot.

The application is organized into layers:

```text
backend/src/main/java/org/hobynye/thankyoumatcher/
├── model/
├── rules/
├── engine/
├── solver/
├── parser/
├── service/
├── controller/
└── config/