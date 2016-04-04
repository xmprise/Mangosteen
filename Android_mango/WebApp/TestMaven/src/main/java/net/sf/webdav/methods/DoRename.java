package net.sf.webdav.methods;

import java.io.IOException;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavConfig;
import net.sf.webdav.WebdavScan;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.AccessDeniedException;
import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.exceptions.ObjectAlreadyExistsException;
import net.sf.webdav.exceptions.ObjectNotFoundException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.fromcatalina.RequestUtil;
import net.sf.webdav.locking.ResourceLocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoRename extends AbstractMethod {
	private static final Logger LOG = LoggerFactory.getLogger(DoRename.class);
	private IWebdavStore _store;
	private ResourceLocks _resourceLocks;
	private boolean _readOnly;

	public DoRename(IWebdavStore store, ResourceLocks resourceLocks,
			boolean readOnly) {
		this._store = store;
		this._resourceLocks = resourceLocks;
		this._readOnly = readOnly;
	}

	public boolean overlapped(ITransaction transaction, HttpServletRequest req,
			HttpServletResponse resp, boolean referencing) throws IOException,
			LockFailedException {
		return false;
	}

	public void executeOverlapped(ITransaction transaction,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, LockFailedException {
	}

	public void execute(ITransaction transaction, HttpServletRequest req,
			HttpServletResponse resp) throws IOException, LockFailedException {
		LOG.trace("-- " + getClass().getName());

		String path = getRelativePath(req);

		if (path.getBytes().length > 255) {
			if (WebdavConfig._debug) {
				LOG.trace("DoRename path :" + path);
			}
			resp.sendError(400);
			return;
		}

		if (!this._readOnly) {
			String tempLockOwner = "doRename" + System.currentTimeMillis()
					+ req.toString();
			if (this._resourceLocks.lock(transaction, path, tempLockOwner,
					false, 0, 10, true))
				try {
					if (!renameResource(transaction, req, resp))
						return;
					WebdavScan.scan();
				} catch (AccessDeniedException e) {
					resp.sendError(403);
				} catch (ObjectAlreadyExistsException e) {
					resp.sendError(409, req.getRequestURI());
				} catch (ObjectNotFoundException e) {
					resp.sendError(404, req.getRequestURI());
				} catch (WebdavException e) {
					resp.sendError(500);
				} finally {
					this._resourceLocks.unlockTemporaryLockedObjects(
							transaction, path, tempLockOwner);
				}
			else
				resp.sendError(500);
		} else {
			resp.sendError(403);
		}
	}

	public boolean renameResource(ITransaction transaction,
			HttpServletRequest req, HttpServletResponse resp)
			throws WebdavException, IOException, LockFailedException {
		String destinationPath = parseDestinationHeader(req, resp);

		if (destinationPath == null) {
			return false;
		}
		String path = getRelativePath(req);

		if (path.equals(destinationPath)) {
			resp.sendError(200);
			return false;
		}

		Hashtable errorList = new Hashtable();
		String parentDestinationPath = getParentPath(getCleanPath(destinationPath));

		if (!checkLocks(transaction, req, resp, this._resourceLocks,
				parentDestinationPath)) {
			errorList.put(parentDestinationPath, Integer.valueOf(423));
			sendReport(req, resp, errorList);
			return false;
		}

		String lockOwner = "renameResource" + System.currentTimeMillis()
				+ req.toString();

		if (this._resourceLocks.lock(transaction, destinationPath, lockOwner,
				false, 0, 10, true)) {
			StoredObject destinationSo = null;
			try {
				StoredObject parentSo = this._store.getStoredObject(
						transaction, path);

				if (parentSo == null) {
					resp.sendError(404);
					int i = 0;
					//return i;
				}
				if (parentSo.isNullResource()) {
					String methodsAllowed = DeterminableMethod
							.determineMethodsAllowed(parentSo);
					resp.addHeader("Allow", methodsAllowed);
					resp.sendError(405);
					int j = 0;
					//return j;
				}
				errorList = new Hashtable();

				destinationSo = this._store.getStoredObject(transaction,
						destinationPath);

				String[] children = this._store.getChildrenNames(transaction,
						parentDestinationPath);
				children = children == null ? new String[0] : children;
				String lastName = getChildPathName(destinationPath);
				for (int i = 0; i < children.length; i++) {
					if (lastName.equals(children[i])) {
						resp.sendError(403);
						int k = 0;
						//return k;
					}
				}
				if (!this._store.renameObject(transaction, path,
						destinationPath)) {
					resp.sendError(404);
					int i = 0;
					//return i;
				}
				resp.setStatus(200);
				WebdavStatus.addSuccessBody2Response(resp, "RENAME");

				if (!errorList.isEmpty())
					sendReport(req, resp, errorList);
			} finally {
				this._resourceLocks.unlockTemporaryLockedObjects(transaction,
						destinationPath, lockOwner);
			}
		} else {
			resp.sendError(500);
			return false;
		}
		return true;
	}

	private String parseDestinationHeader(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String destinationPath = req.getHeader("Allow-rename");

		if (destinationPath == null) {
			resp.sendError(400);
			return null;
		}

		destinationPath = RequestUtil.URLDecode(destinationPath, "UTF8");

		int protocolIndex = destinationPath.indexOf("://");
		if (protocolIndex >= 0) {
			int firstSeparator = destinationPath
					.indexOf("/", protocolIndex + 4);
			if (firstSeparator < 0)
				destinationPath = "/";
			else
				destinationPath = destinationPath.substring(firstSeparator);
		} else {
			String hostName = req.getServerName();
			if ((hostName != null) && (destinationPath.startsWith(hostName))) {
				destinationPath = destinationPath.substring(hostName.length());
			}

			int portIndex = destinationPath.indexOf(":");
			if (portIndex >= 0) {
				destinationPath = destinationPath.substring(portIndex);
			}

			if (destinationPath.startsWith(":")) {
				int firstSeparator = destinationPath.indexOf("/");
				if (firstSeparator < 0)
					destinationPath = "/";
				else {
					destinationPath = destinationPath.substring(firstSeparator);
				}
			}

		}

		destinationPath = normalize(destinationPath);

		String contextPath = req.getContextPath();
		if ((contextPath != null) && (destinationPath.startsWith(contextPath))) {
			destinationPath = destinationPath.substring(contextPath.length());
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo != null) {
			String servletPath = req.getServletPath();
			if ((servletPath != null)
					&& (destinationPath.startsWith(servletPath))) {
				destinationPath = destinationPath.substring(servletPath
						.length());
			}

		}

		return destinationPath;
	}

	protected String normalize(String path) {
		if (path == null) {
			return null;
		}

		String normalized = path;

		if (normalized.equals("/.")) {
			return "/";
		}

		if (normalized.indexOf('\\') >= 0)
			normalized = normalized.replace('\\', '/');
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		while (true) {
			int index = normalized.indexOf("//");
			if (index < 0)
				break;
			normalized = normalized.substring(0, index)
					+ normalized.substring(index + 1);
		}

		while (true) {
			int index = normalized.indexOf("/./");
			if (index < 0)
				break;
			normalized = normalized.substring(0, index)
					+ normalized.substring(index + 2);
		}

		while (true) {
			int index = normalized.indexOf("/../");
			if (index < 0)
				break;
			if (index == 0)
				return null;
			int index2 = normalized.lastIndexOf('/', index - 1);
			normalized = normalized.substring(0, index2)
					+ normalized.substring(index + 3);
		}

		return normalized;
	}
}