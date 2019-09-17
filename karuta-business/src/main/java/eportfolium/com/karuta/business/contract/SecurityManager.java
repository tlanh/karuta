package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface SecurityManager {

	/**
	 * 
	 * This method provides a way for security officers to"reset" the userPassword.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	boolean changePassword(String username, String password);

	void changeUserPassword(Long userId, String currentPassword, String newPassword) throws BusinessException;

	boolean registerUser(String username, String password);

	Long createUser(String username, String email) throws BusinessException;

	boolean createUser(String username, String email, boolean isDesigner, long userId) throws Exception;

	String generatePassword();

	int deleteCredential(Long userId) throws BusinessException;

	int deleteUsers(Long userId, Long groupId);

	void changeCustomer(Credential user) throws BusinessException;

	String userChangeInfo(Long userId, Long userId2, String in) throws BusinessException;

	boolean isAdmin(Long id);

	boolean isCreator(Long id);

	/**
	 * Check if customer password is the right one
	 *
	 * @param passwd Password
	 * @return bool result
	 */
	boolean checkPassword(Long userID, String passwd);

	String putInfUser(Long userId, long userId2, String xmlUser) throws BusinessException;

}
