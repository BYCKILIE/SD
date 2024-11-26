import React, { useState } from "react";
import "./UsersList.css";
import useUsers from "../../hooks/useUsers.js";
import {useNavigate} from "react-router-dom";

const UsersList = () => {
    const [expandedUser, setExpandedUser] = useState(null);
    const [searchQuery, setSearchQuery] = useState("");
    const [xmlResp, setXmlResp] = useState([]);

    const {fetchUsers} = useUsers();

    const offset = () => {
        if (searchQuery === "" || searchQuery.length < 2) {
            return 0;
        }
        setExpandedUser(null);
        return -1;
    }

    const getUsers = async () => {
        try {
            const users = await fetchUsers(searchQuery, offset());
            if (!users) {
                setXmlResp([])
            } else if (Array.isArray(users)) {
                setXmlResp(users);
            } else {
                setXmlResp([users]);
            }
        } catch {
            setXmlResp([]);
        }
    };

    useState(() => {
        const handleUsers = async () => {
            await getUsers();
        }

        handleUsers();
    })

    const handleExpandClick = (email) => {
        setExpandedUser(expandedUser === email ? null : email);
    };

    const handleSearchChange = (event) => {
        setSearchQuery(event.target.value.toLowerCase());
        getUsers();
    };

    return (
        <div className="users-list-container">
            <input
                type="text"
                placeholder="Search by name..."
                className="search-bar"
                value={searchQuery}
                onChange={handleSearchChange}
            />

            <div className="users">
                {xmlResp && xmlResp.length > 0 ? (
                    xmlResp.map((user, index) => (
                        <User
                            key={index}
                            email={user.email}
                            firstName={user.firstName}
                            lastName={user.lastName}
                            profilePicUrl={`https://sd.users.bchportal.net/assets/profile_pictures${user.profilePicUrl}`}
                            isExpanded={expandedUser === user.email}
                            onClick={() => handleExpandClick(user.email)}
                        />
                    ))
                ) : (
                    <p>No users found.</p>
                )}
            </div>
        </div>
    );
};

// eslint-disable-next-line react/prop-types
const User = ({ email, firstName, lastName, profilePicUrl, isExpanded, onClick }) => {
    const navigate = useNavigate();

    const { promoteUser, deleteAdminUser } = useUsers();

    const onSubmit = () => {
        navigate("/admin/user?email=" + email);
    }

    const onAdmin = () => {
        promoteUser(email);
    }

    const onDelete = () => {
        deleteAdminUser(email);
    }

    return (
        <div className={`user-container ${isExpanded ? "expanded" : ""}`} onClick={onClick}>
            <img src={profilePicUrl} alt={`${firstName}'s profile`} className="profile-picture" />
            <div className="user-info">
                <p><strong>{email}</strong></p>
                <p><strong>First Name:</strong> {firstName}</p>
                <p><strong>Last Name:</strong> {lastName}</p>
            </div>
            {isExpanded && (
                <div className="expanded-content">
                    <button className="details-button" onClick={onSubmit}>Edit</button>
                    <button className="promote-button" onClick={onAdmin}>Promote</button>
                    <button className="delete-button" onClick={onDelete}>Delete</button>
                </div>
            )}
        </div>
    );
};

export default UsersList;
