package com.example.Interview.company;

import com.example.Interview.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Company not found", HttpStatus.NOT_FOUND));
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public Company updateCompany(Long id, Company details) {
        Company company = getCompany(id);
        company.setName(details.getName());
        company.setRequiredSkills(details.getRequiredSkills());
        company.setMinimumScore(details.getMinimumScore());
        company.setDifficulty(details.getDifficulty());
        return companyRepository.save(company);
    }
}
