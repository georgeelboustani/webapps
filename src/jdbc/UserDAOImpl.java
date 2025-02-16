package jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pagebeans.UpdateBean;
import exceptions.ServiceLocatorException;

/* Admin Manual insert
 * insert into public.user (username,nickname,first_name,last_name,password,email,year_of_birth,activate,ban,is_admin) values ('admin','','user1','','5f4dcc3b5aa765d61d8327deb882cf99','blah@hotmail.com','3910-08-22','true','false','true');
 */

public class UserDAOImpl implements UserDAO {
	
	@Override
	public void addUser(UserDTO user) throws SQLException {
		Connection con = null;
		try {
			con = DBConnectionFactory.getConnection();
			
			PreparedStatement updateUser = con.prepareStatement("INSERT into " + DBUtils.SCHEMA_NAME + ".user "
															 + "(username,nickname,first_name,last_name,password,"
															 + "email,year_of_birth,activate,ban,credit_card_num,activate_hashsum,is_admin,address)"
															 + " values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			updateUser.setString(1,user.getUsername());
			updateUser.setString(2,user.getNickname());
			updateUser.setString(3,user.getFirstName());
			updateUser.setString(4,user.getLastName());
			updateUser.setString(5,DBUtils.calculateMD5(user.getPassword()));
			updateUser.setString(6,user.getEmail());
			updateUser.setDate(7,user.getYearOfBirth());
			updateUser.setBoolean(8,user.getActivated());
			updateUser.setBoolean(9,user.getBanned());
			updateUser.setString(10,user.getCreditCardNum());
			updateUser.setString(11,user.getCheckSum());
			updateUser.setBoolean(12, false);
			updateUser.setString(13,user.getAddress());
			
			updateUser.executeUpdate();      

		} catch (ServiceLocatorException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}
	
	// TODO - not tested yet
	@Override
	public UserDTO getUserById(int userId) throws SQLException {
		UserDTO user = null;
		Connection con = null;
		try {
			con = DBConnectionFactory.getConnection();
			
			PreparedStatement userQuery = con.prepareStatement("SELECT * FROM " + DBUtils.SCHEMA_NAME + "." + DBUtils.USER_TABLE
															 + " WHERE uid=?");
			userQuery.setInt(1,userId);
			
			ResultSet rs = userQuery.executeQuery();
			if (rs.next()) {
				user = generateUserDTO(rs);
			}
		} catch (ServiceLocatorException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
		
		return user;
	}

	// TODO - not tested yet
	@Override
	public UserDTO getUserByUserName(String username) throws SQLException {
		UserDTO user = null;
		
		Connection con = null;
		
		try {
			con = DBConnectionFactory.getConnection();
			
			PreparedStatement userQuery = con.prepareStatement("SELECT * FROM " + DBUtils.SCHEMA_NAME + "." + DBUtils.USER_TABLE
															 + " WHERE "+DBUtils.USER_NAME+"=?");
			userQuery.setString(1, username);
			
			ResultSet rs = userQuery.executeQuery();
			if (rs.next()) {
				user = generateUserDTO(rs);
			}
			
		} catch (ServiceLocatorException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
		
		return user;
	}
	
	// TODO - not tested yet
		@Override
		public int getNumUserByUserName(String username) throws SQLException {
			int returnNumber = 0;
			Connection con = null;
			
			try {
				con = DBConnectionFactory.getConnection();
				
				PreparedStatement userQuery = con.prepareStatement("SELECT COUNT(*) AS result FROM " + DBUtils.SCHEMA_NAME + "." + DBUtils.USER_TABLE
																 + " WHERE username=?");
				
				userQuery.setString(1, username);
				
				ResultSet rs = userQuery.executeQuery();
				if(rs.next()){
					returnNumber = rs.getInt("result");
				}
				
			} catch (ServiceLocatorException e) {
				// TODO do some roll back probably
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO do some roll back probably
				e.printStackTrace();
			} finally {
				if (con != null) {
					con.close();
				}
			}
			
			return returnNumber;
		}

	// TODO - not tested yet
	@Override
	public List<UserDTO> getAllUsers() throws SQLException {
		List<UserDTO> users = new ArrayList<UserDTO>();
		
		Connection con = null;
		
		try {
			con = DBConnectionFactory.getConnection();
			
			PreparedStatement userQuery = con.prepareStatement("SELECT * FROM " + DBUtils.SCHEMA_NAME + "." + DBUtils.USER_TABLE);
			
			ResultSet rs = userQuery.executeQuery();
			while (rs.next()) {
				users.add(generateUserDTO(rs));
			}
			
		} catch (ServiceLocatorException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
		
		return users;
	}
	
	// TODO - test this out
	@Override
	public int authenticateLogin(String username, String password) throws SQLException {
		int resultUid = -1;
		
		Connection con = null;
		
		try {
			con = DBConnectionFactory.getConnection();
			PreparedStatement userQuery = con.prepareStatement("SELECT uid FROM " + DBUtils.SCHEMA_NAME + "." + DBUtils.USER_TABLE 
															 + " WHERE "+DBUtils.USER_NAME+"=? AND "+DBUtils.USER_PASSWORD+"=? "
															 		+ "AND "+DBUtils.USER_ACTIVE+"=TRUE AND "+DBUtils.USER_BAN+"=FALSE;");
			// TODO - should make username unique in the database
			
			userQuery.setString(1, username);
			userQuery.setString(2, DBUtils.calculateMD5(password));
			
			// If anything is returned, then the login username and pass is a valid combination
			ResultSet rs = userQuery.executeQuery();
			if(rs.next()){
				resultUid = rs.getInt("uid");
			}
		} catch (ServiceLocatorException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
		
		return resultUid;
	}
	
	
	@Override
	public void updateUser(UpdateBean updateBean) throws SQLException {
		Connection con = null;
		try {
			con = DBConnectionFactory.getConnection();
			
			PreparedStatement updateUser = con.prepareStatement("UPDATE " + DBUtils.SCHEMA_NAME + ".user "
															 + "SET nickname=?,first_name=?,last_name=?,"
															 + "password=?,email=?,year_of_birth=?,"
															 + "credit_card_num=?,address=? "
															 + "WHERE " + DBUtils.USER_ID + " = " + updateBean.getUid());
			updateUser.setString(1,updateBean.getNickname());
			updateUser.setString(2,updateBean.getFirstName());
			updateUser.setString(3,updateBean.getLastName());
			updateUser.setString(4,DBUtils.calculateMD5(updateBean.getPassword()));
			updateUser.setString(5,updateBean.getEmail());
			updateUser.setDate(6,updateBean.getYearOfBirth());
			updateUser.setString(7,updateBean.getCreditCardNum());
			updateUser.setString(8,updateBean.getAddress());
			
			updateUser.executeUpdate();      

		} catch (ServiceLocatorException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}
	
	private UserDTO generateUserDTO(ResultSet rs) throws SQLException {
		UserDTO user = new UserDTO();
		
		user.setUid(rs.getInt(DBUtils.USER_ID));
		user.setUsername(rs.getString(DBUtils.USER_NAME));
		user.setNickname(rs.getString(DBUtils.USER_NICKNAME));
		user.setFirstName(rs.getString(DBUtils.USER_FIRST_NAME));
		user.setLastName(rs.getString(DBUtils.USER_LAST_NAME));
		user.setPassword(rs.getString(DBUtils.USER_PASSWORD));
		user.setEmail(rs.getString(DBUtils.USER_EMAIL));
		user.setYearOfBirth(rs.getDate(DBUtils.USER_DOB));
		
		user.setActivated(rs.getBoolean(DBUtils.USER_ACTIVE));
		user.setBanned(rs.getBoolean(DBUtils.USER_BAN));
		user.setCreditCardNum(rs.getString(DBUtils.USER_CREDIT_CARD_NUM));
		user.setCheckSum(rs.getString(DBUtils.USER_CHECKSUM));
		user.setIsAdmin(rs.getBoolean(DBUtils.USER_IS_ADMIN));
		user.setAddress(rs.getString(DBUtils.USER_ADDRESS));
		
		return user;
	}

	@Override
	public void banUser(int userId) throws SQLException {
		Connection con = null;
		try {
			con = DBConnectionFactory.getConnection();
			
			PreparedStatement updateUser = con.prepareStatement("UPDATE " + DBUtils.SCHEMA_NAME + ".user "
															 + "SET ban=?"
															 + "WHERE " + DBUtils.USER_ID + " = " + userId);
			updateUser.setBoolean(1,true);
			
			updateUser.executeUpdate();      

		} catch (ServiceLocatorException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}

	@Override
	public void activateUser(String username, String checksum)
			throws SQLException {
		
		Connection con = null;
		try {
			con = DBConnectionFactory.getConnection();
			
			PreparedStatement updateUser = con.prepareStatement("UPDATE " + DBUtils.SCHEMA_NAME + ".user "
															 + "SET activate = ? "
															 + "WHERE " + DBUtils.USER_NAME + " = ? AND " 
															 + DBUtils.USER_CHECKSUM + " = ? ;");
			updateUser.setBoolean(1, true);
			updateUser.setString(2, username);
			updateUser.setString(3, checksum);
			
			updateUser.executeUpdate();      

		} catch (ServiceLocatorException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO do some roll back probably
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
		
	}

}