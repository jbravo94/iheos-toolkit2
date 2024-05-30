package edu.wustl.mir.erl.ihe.xdsi.validation;

import edu.wustl.mir.erl.ihe.xdsi.validation.DCMAssertion.TYPE;
import org.dcm4che3.data.Tag;

import java.util.ArrayList;

@SuppressWarnings("javadoc")
public class DetailDcmKOSContent extends DetailDcmContent {

   
   @Override
   protected void initializeTests() {
      desc = "KOS Document";
      assertions = new ArrayList <>();
      // SOP Class UID (0008,0016)
      assertions.add(new DCMAssertion(TYPE.SAME, Tag.SOPClassUID));
      // SOP Instance UID (0008,0018)
      assertions.add(new DCMAssertion(TYPE.DIFFERENT, Tag.SOPInstanceUID));
      // Patient Name (0010,0010)
      assertions.add(new DCMAssertion(TYPE.PRESENT, Tag.PatientName));
      assertions.add(new DCMAssertion(TYPE.SAME, Tag.PatientName));
      // Patient ID (0010,0020)
      assertions.add(new DCMAssertion(TYPE.PRESENT, Tag.PatientID));
      assertions.add(new DCMAssertion(TYPE.SAME, Tag.PatientID, "", CAT.SUCCESS, CAT.WARNING));
      // Patient Birth Date (0010,0030)
      assertions.add(new DCMAssertion(TYPE.SAME, Tag.PatientBirthDate));
      // Patient Sex (0010,0040)
      assertions.add(new DCMAssertion(TYPE.SAME, Tag.PatientSex));
      // Accession Number (0008,0050)
      assertions.add(new DCMAssertion(TYPE.PRESENT, Tag.AccessionNumber));
      assertions.add(new DCMAssertion(TYPE.SAME, Tag.AccessionNumber, "", CAT.SUCCESS, CAT.WARNING));
      // Modality (0008,0060)
      assertions.add(new DCMAssertion(TYPE.PRESENT, Tag.Modality, "KO"));
      assertions.add(new DCMAssertion(TYPE.CONSTANT, Tag.Modality, "KO"));
      // Study Instance UID (0020,000D)
      assertions.add(new DCMAssertion(TYPE.SAME, Tag.StudyInstanceUID, "", CAT.SUCCESS, CAT.WARNING));
      // Series Instance UID (0020,000E)
      assertions.add(new DCMAssertion(TYPE.DIFFERENT, Tag.SeriesInstanceUID));
      // Content Sequence size (0040,A730)
      //assertions.add(new DCMAssertion(TYPE.SAME_SIZE, Tag.ContentSequence));
   }
}
