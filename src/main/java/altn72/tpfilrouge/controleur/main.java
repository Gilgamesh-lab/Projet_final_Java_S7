package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.EquipementRepository;
import altn72.tpfilrouge.service.EquipementService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class main {
    public static void main(String[] args) {
        ProgrammeurControleur repository = new ProgrammeurControleur(new EquipementService());


        }
    }
}
