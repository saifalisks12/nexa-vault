package com.nexavault.service;

import com.nexavault.requestdto.UploadFileRequestDto;
import com.nexavault.responsedto.UploadFileResponseDto;

public interface IpfsService {

	UploadFileResponseDto uploadToIPFS(UploadFileRequestDto request);

	byte[] downloadAndDecrypt(String ipfsHash);

	void deleteFromIPFS(String ipfsHash);

}
