# AntibiogramJ_XML_Creator
Abstract: Transforms an xls file in the format of the EUCAST to an XML file for the AntibiogramJ program.

As the abstract says the program supports translation of an EUCAST type (similar format) xls (or xlsx) to an XML for the AntibiogramJ program (available at https://sourceforge.net/projects/antibiogramj/).

For a given xls document it generates an XML with a structure similar to:

- standard
  - metadata
    - name
    - version
    - validFrom
    - susceptibleGTorG
    - resistentLTorL
  - breakpoints
    - familyBreakpoint*
      - bacteriaFamily
      - breakpoint*
        - antibiotic
          - name
          - family
          - diskContent
        - case
        - available
        - susceptible
        - resistant
        - comments

# To be done:

The program still has some flaws that can be corrected...

- Try to get the comments(notes) of each antibiotic. In the EUCAST format they are referenced using superindexes using commas, letters and numbers, all of that difficults the task as, for example, a 1 can have a 4 as a superindex and Java gets that as a 14.

- Try to get the data from the last sheet (of bacterias, M. Tuberculosis). The program searches for antibiotic family names and then gets all antibiotics for that family but M. Tuberculosis does not even use Miscellaneus agents as a family. 

> ... but these are not very important as the M. Tuberculosis sheet does not provide us with any onformation of "Zone diameter breakpoints", the information that is used for AntibiogramJ and the notes are merely that, explanations for a number or a lack of information.
  
