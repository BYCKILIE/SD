import { useContext } from 'react';
import ProfilesContext from "../context/ProfilesContext.jsx";

const useProfiles = () => {
    return useContext(ProfilesContext);
};

export default useProfiles;