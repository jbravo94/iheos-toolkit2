package gov.nist.toolkit.session.server;

import gov.nist.toolkit.utilities.io.Io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DirectConfigManager {
	String externalCache;
	String sep = File.separator;
	
	static Logger logger = Logger.getLogger(DirectConfigManager.class);

	
	public DirectConfigManager(File externalCache) {
		this.externalCache = externalCache + sep;
	}

	public String pathToDirectConfigArea() {
		return externalCache + "direct";
	}

	public String pathToContactFile() {
		return pathToDirectConfigArea() +
				File.separator + "contact" + sep;
	}

	public String pathToSigningCertDir() {
		return pathToDirectConfigArea() + File.separator + "signing_cert";
	}
	
	public String pathToEncryptionCertsDir() {
		return pathToDirectConfigArea() + File.separator + "encrypt_certs";
	}
	
	/**
	 * File must have .p12 suffix
	 * @return
	 */
	public File getSigningCertFile() {
		File certDir = new File(pathToSigningCertDir());
		if (!certDir.exists() || !certDir.isDirectory())
			return null;
		String[] files = certDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".p12");
			}
		});
		if (files.length == 1)
			return new File(certDir + sep + files[0]);
		return null;
	}
	
	public File getEncryptionCertFile(String domain) {
		final String theDomain = domain;
		File certDir = new File(pathToEncryptionCertsDir());
		logger.info("Looking for encryption certs for domain " + domain + " in " + certDir + " ...");
		if (!certDir.exists() || !certDir.isDirectory()) {
			logger.error("   ... NOT FOUND");
			return null;
		}
		String[] files = certDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith(theDomain);
			}
		});
		ArrayList<String> fileL = new ArrayList<String>();
		for (int i=0; i<files.length; i++)
			fileL.add(files[i]);
		logger.info("... found " + fileL);
		if (files.length == 1)
			return new File(certDir + sep + files[0]);
		return null;
	}
	
	public byte[] getSigningCert() {
		File file = getSigningCertFile();
		if (file == null)
			return null;
		try {
			return Io.bytesFromFile(file);
		} catch (IOException e) {
			return null;
		}
	}

	public String getSigningCertPassword() {
		File certDir = new File(pathToSigningCertDir());
		if (!certDir.exists() || !certDir.isDirectory())
			return null;
		String[] files = certDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.equals("password.txt");
			}
		});
		if (files.length == 1) {
			try {
				return Io.stringFromFile(new File(certDir + sep + files[0])).trim();
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
	
	/**
	 * Return list of domain names for which encryption certs are pre-loaded
	 * @return
	 */
	public List<String> getEncryptionCertDomains() {
		List<String> domains = new ArrayList<String>();
		
		File eCertsDir = new File(pathToEncryptionCertsDir());
		if (!eCertsDir.exists() || !eCertsDir.isDirectory())
			return domains;
		String[] files = eCertsDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".der");
			}
		});
		
		for (int i=0; i<files.length; i++) {
			String file = files[i];
			String domain = file.substring(0, file.lastIndexOf(".der"));
			domains.add(domain);
		}
		
		logger.info("Found encryption certs for domains " + domains);
		
		return domains;
	}
}