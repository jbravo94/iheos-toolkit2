RAD-69 Retrieve: Single Image Study
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html;
      charset=windows-1252">
    <title>Imaging Document Consumer RAD-69 Retrieve Imaging Document
      Set: Single Image Study</title>
  </head>
  <body>
    <h2>Imaging Document Consumer RAD-69 Retrieve Imaging Document Set:
      Single Image Study</h2>
    <h3>Purpose / Context</h3>
    The Imaging Document Consumer performs:
    <ul>
      <li>RAD-69 Retrieve Imaging Document Set to retrieve the study for
        the specified image.</li>
    </ul>
    The test points are:
    <ul>
      <li>The Imaging Document Consumer can perform a valid RAD-69
        Retrieve Images independent of other transactions.</li>
    </ul>
    <h3>Test Steps</h3>
    <i>Setup</i>
    <ul>
      <li>Images and KOS objects are pre-loaded into the testing system
        actor simulators.</li>
      <li>The Imaging Document Consumer will query the actor simulators
        directly.</li>
    </ul>
    <i>Instructions</i>
    <ol>
      <li>The Imaging Document Consumer is instructed to query the
        Registry/Repository simulator for the KOS object for the patient
        with patient ID: <br>
        IDCAD013-a^^^&amp;1.3.6.1.4.1.21367.2005.13.20.1000&amp;ISO</li>
      <li>The Imaging Document Consumer should both query for and
        retrieve the KOS object.</li>
      <li>The Imaging Document Consumer is instructed to use a RAD-69
        Retrieve Imaging Document Set) transaction to retrieve the
        imaging study with one image from the Imaging Document Source
        simulator.</li>
    </ol>
    <i>Validation</i>
    <ol>
      <li>Test Manager: Detailed instructions on validating the query
        and retrieve of the KOS are outlined in the test: ids_4831
        'Retrieve KOS: Single Image Study'. Follow verification steps 1
        through 6.</li>
      <li>Test Manager: Verify that the retrieved KOS object is the
        correct one. It should contain the following attributes:
        <table>
          <tbody>
            <tr>
              <td>(0008,0018) UI #44
                [1.3.6.1.4.1.21367.201599.3.201603032140034.1] </td>
              <td>SOPInstanceUID</td>
            </tr>
            <tr>
              <td>(0008,0050) SH #6 [IDC013-a]</td>
              <td>AccessionNumber</td>
            </tr>
            <tr>
              <td>(0010,0010) PN #12 [Single^Soap-a]</td>
              <td>PatientName</td>
            </tr>
            <tr>
              <td>(0010,0020) LO #10 [IDCDEPT013-a]</td>
              <td>PatientID</td>
            </tr>
          </tbody>
        </table>
      </li>
      <li>Test Manager: Locate the RAD-69 request message in the
        simulator logs.</li>
      <li>Test Manager: Take a snapshot of the evidence.</li>
      <li>Test Manager: Verify by hand that the request is of the format
        listed below. Verify that these elements are present in the
        request:
        <ol type="a">
          <li>StudyRequest
            <ol type="i">
              <li>SeriesRequest
                <ol>
                  <li>DocumentRequest</li>
                </ol>
              </li>
            </ol>
          </li>
          <li>TransferSyntaxUIDList
            <ol type="i">
              <li>TransferSyntaxUID</li>
            </ol>
          </li>
        </ol>
      </li>
    </ol>
    <textarea style="border:none;" rows="17" cols="100"
      disabled="disabled"> &lt;iherad:RetrieveImagingDocumentSetRequest
      &nbsp;xmlns:iherad="urn:ihe:rad:xdsi-b:2009"
      &nbsp;xmlns:ihe="urn:ihe:iti:xds-b:2007"
      &nbsp;xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;
      &nbsp;&lt;iherad:StudyRequest
      studyInstanceUID="1.3.6.1.4.1.21367.201599.1.201604021948025"&gt;
      &nbsp;&nbsp;&lt;iherad:SeriesRequest
      seriesInstanceUID="1.3.6.1.4.1.21367.201599.2.201604021948025"&gt;
      &nbsp;&nbsp;&nbsp;&lt;ihe:DocumentRequest&gt;
      &nbsp;&nbsp;&nbsp;&nbsp;&lt;ihe:RepositoryUniqueId&gt;1.3.6.1.4.1.21367.102.1.1&lt;/ihe:RepositoryUniqueId&gt;



      &nbsp;&nbsp;&nbsp;&nbsp;&lt;ihe:DocumentUniqueId&gt;1.3.6.1.4.1.21367.201599.3.201604021948025.1&lt;/ihe:DocumentUniqueId&gt;



      &nbsp;&nbsp;&nbsp;&lt;/ihe:DocumentRequest&gt;
      &nbsp;&nbsp;&lt;/iherad:SeriesRequest&gt;
      &nbsp;&lt;/iherad:StudyRequest&gt;
      &nbsp;&lt;iherad:TransferSyntaxUIDList&gt;
      &nbsp;&nbsp;&lt;iherad:TransferSyntaxUID&gt;1.2.840.10008.1.2.1&lt;/iherad:TransferSyntaxUID&gt;



      &nbsp;&lt;/iherad:TransferSyntaxUIDList&gt;
      &lt;/iherad:RetrieveImagingDocumentSetRequest&gt; </textarea>
  </body>
</html>
