package com.example.application.services;

import com.example.application.data.Artwork;
import com.example.application.data.User;
import com.example.application.repository.UserRepository;
import com.example.application.repository.ArtworkRepository;
import com.example.application.data.StudentInfo;
import com.example.application.repository.StudentInfoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Collections;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;
    private final StudentInfoRepository studentInfoRepository;

    public ArtworkService(ArtworkRepository artworkRepository, StudentInfoRepository studentInfoRepository,
			UserRepository userRepository){
        this.artworkRepository = artworkRepository;
	this.studentInfoRepository = studentInfoRepository;
	this.userRepository = userRepository;
    }

    public void saveArtwork(String email, String artworkUrl, String description){
        User user = userRepository.findByEmail(email);

	DateTimeFormatter formatterr = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
					.withLocale(Locale.US);

	LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));

	String dateTime = formatterr.format(localDateTime);

	LocalTime localTime = LocalTime.now();
	LocalDate localDate = LocalDate.now();

        if (user != null) {
            Artwork artwork = new Artwork();
            artwork.setUser(user);
            artwork.setArtworkUrl(artworkUrl);
            artwork.setDescription(description);
            artwork.setDateTime(dateTime);
            artwork.setTimeOfPost(localTime);
            artwork.setDateOfPost(localDate);

            artworkRepository.save(artwork);
        }
    }

    public void updateArtwork(Artwork artwork){
    	artworkRepository.save(artwork);
    }

    public int getArtworksCountByUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return artworkRepository.countByStudentInfoUser(user);
        }
        return 0;
    }

    public List<Artwork> getAllArtworks(){
	return artworkRepository.findAll();
    }

    public List<Artwork> getArtworksByUserId(Long userId) {
        return artworkRepository.findByUserId(userId);
    }

    public Artwork getArtworkById(Long artworkId){
	return artworkRepository.findById(artworkId).orElse(null);
    }

    public void deleteArtwork(Artwork artwork){
    	artworkRepository.delete(artwork);
    }

    public List<Artwork> searchArtworks(String searchTerm, Long userId){
    	if(searchTerm == null || searchTerm.isEmpty()){
    	   return artworkRepository.findByUserId(userId);
    	}else{
    	   return artworkRepository.search(searchTerm, userId);
    	}
    }
}
